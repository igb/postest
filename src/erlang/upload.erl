-module(upload).
-export([upload/0]).

upload()->
    {ok, Data}=file:read_file("IMG_5303.jpg"), 
    Chunks=chunk(Data, 512),
    upload_append(Chunks, "MediaId", 0, "x", "x", "x", "x").

upload_append([MediaData|T], MediaId, SegmentId, ConsumerKey, ConsumerSecret, AccessToken, AccessTokenSecret)-> 




    Params = [{"command", "APPEND"},
	      {"media_id", MediaId},
	      {"media", base64:decode_to_string(MediaData)},
	      {"segment_index", integer_to_list(SegmentId)}],
    
    Url =  "http://127.0.0.1:8080/postest/uploadtest/",
    MultiPartBody=generate_multipart__body(Params, "--fooo"),


    Headers =  [{"Accept", "*/*"},
		{"Host","twitter.com"},
		{"Content-Type","multipart/form-data; boundary=fooo"},
		{"Authorization",
		 create_oauth_header([], %remove params
				    Url,
				     ConsumerKey,
				     ConsumerSecret,
				     AccessToken, 
				     AccessTokenSecret, 
				     get_oauth_nonce(),
				     get_oauth_timestamp(), 
				     "Post")
		},
	       {"Content-Length", integer_to_list(length(MultiPartBody))}],

    
    io:format("~s~n", [generate_multipart__body(Params, "--fooo")]),
    {ok, Response} = httpc:request(post,
				   {Url,
				    Headers,
				    "multipart/form-data",
				    MultiPartBody
				   }, [], [{headers_as_is, true}]),
    io:format("~p~n", [Response]),
    "x" = Response;
%    upload_append(T, MediaId, SegmentId + 1, ConsumerKey, ConsumerSecret, AccessToken, AccessTokenSecret);

upload_append([], MediaId, _, _, _,_,_)-> 
    {ok, MediaId}.



%roll yer own oauth

escape_uri([C | Cs]) when C >= $a, C =< $z ->
%    io:format("a-z:~p~n",[C]),
    [C | escape_uri(Cs)];
escape_uri([C | Cs]) when C >= $A, C =< $Z ->
    [C | escape_uri(Cs)];
escape_uri([C | Cs]) when C >= $0, C =< $9 ->
    [C | escape_uri(Cs)];
escape_uri([C = $. | Cs]) ->
    [C | escape_uri(Cs)];
escape_uri([C = $- | Cs]) ->
    [C | escape_uri(Cs)];
escape_uri([C = $_ | Cs]) ->
    [C | escape_uri(Cs)];
escape_uri([C = $* | Cs]) ->
    [C | escape_uri(Cs)];
escape_uri([C | Cs]) when C > 16#7f ->
    HexStr = integer_to_list(C, 16),
    lists:flatten([$%, HexStr]) ++ escape_uri(Cs);
escape_uri([C | Cs]) ->
    escape_byte(C) ++ escape_uri(Cs);
escape_uri([]) ->
    [].

escape_byte(C) when C >= 0, C =< 255 ->
    [$%, hex_digit(C bsr 4), hex_digit(C band 15)].

hex_digit(N) when N >= 0, N =< 9 ->
    N + $0;
hex_digit(N) when N > 9, N =< 15 ->
    N + $A - 10.


create_oauth_header(RequestParameters, Url, ConsumerKey, ConsumerSecret, OauthToken, OauthTokenSecret, OauthNonce, OauthTimestamp, HttpMethod)->
    
    OauthSignatureMethod = "HMAC-SHA1",
    OauthVersion = "1.0",
    
    
    OauthParameters = [ {"oauth_consumer_key", ConsumerKey},
			{"oauth_nonce",OauthNonce},
			{"oauth_signature_method", OauthSignatureMethod},
			{"oauth_timestamp", OauthTimestamp},
			{"oauth_token", OauthToken},
			{"oauth_version",OauthVersion}],
    SigningParameters = lists:append(OauthParameters, RequestParameters),
    OauthSignature = sign(SigningParameters, Url, ConsumerSecret, OauthTokenSecret, HttpMethod),
    SignedOauthParameters = lists:append(OauthParameters, [{"oauth_signature", OauthSignature}]),
    create_oauth_header_string(SignedOauthParameters).

    


sign(Parameters, Url, ConsumerSecret, OauthTokenSecret, HttpMethod)->
    ParameterString = create_parameter_string(Parameters),
    SignatureBaseString = create_signature_base_string(ParameterString, Url, HttpMethod),
    SigningKey= get_signing_key(ConsumerSecret, OauthTokenSecret),
    io:format("~p~n",[crypto:hmac(sha, SigningKey, SignatureBaseString)]),
    base64:encode_to_string(crypto:hmac(sha, SigningKey, SignatureBaseString)).

create_parameter_string(Parameters)->
    EncodedParameters = encode_parameters(Parameters),
    SortedEncodedParamters = lists:keysort(1, EncodedParameters),
    lists:foldl(fun({X, Y}, Acc) ->
			case Acc of
			    [] ->
				string:concat(string:concat(X, "="), Y);
			    _ -> string:concat(Acc, string:concat(string:concat(string:concat("&", X), "="), Y))
			end
		end,
		[],
		SortedEncodedParamters).


create_oauth_header_string(Parameters)->
    EncodedParameters = encode_parameters(Parameters),
    SortedEncodedParamters = lists:keysort(1, EncodedParameters),
    lists:foldl(fun({X, Y}, Acc) ->
			case Acc of
			    "OAuth " ->
				string:concat(Acc, string:concat(string:concat(X, "="), string:concat(string:concat("\"", Y), "\"")));
			    _ -> string:concat(Acc, string:concat(string:concat(string:concat(", ", X), "="), string:concat(string:concat("\"", Y), "\"")))
			end
		end,
		"OAuth ",
		SortedEncodedParamters).
			

create_signature_base_string(ParameterString, Url, HttpMethod)->

    UpperCaseHttpMethod = string:to_upper(HttpMethod),
    SignatureBaseStringPrefix = string:concat(UpperCaseHttpMethod, string:concat("&", escape_uri(Url))),
    string:concat(string:concat(SignatureBaseStringPrefix, "&"), escape_uri(ParameterString)).
    
    
get_signing_key(ConsumerSecret, OauthTokenSecret)->					      
    string:concat(string:concat(escape_uri(ConsumerSecret), "&"), escape_uri(OauthTokenSecret)).
    
  
encode_parameters(Parameters)->
    lists:map(fun({X,Y}) ->
		      {escape_uri(key_to_string(X)), escape_uri(key_to_string(Y))}
	      end,
	      Parameters).

 
current_time_millis()->   
    {Mega, Sec, Micro} = os:timestamp(),
    (Mega*1000000 + Sec)*1000 + round(Micro/1000).

current_time_seconds()->
    {MegaSecs, Secs, MicroSecs} = os:timestamp(),
    MegaSecs * 1000000 + Secs.

get_oauth_timestamp()->
    lists:flatten(io_lib:format("~p", [current_time_seconds()])).

get_oauth_nonce()->
    base64:encode_to_string(list_to_binary(integer_to_list(current_time_millis()))).

add_params_to_url(Url, Params)->
    append_params_to_url( string:concat(Url, "?"), Params).

append_params_to_url(Url, [H|T])->  
    case lists:last(Url) of
	63 ->
	    NewUrlBase = Url;
	_ ->
	    NewUrlBase = string:concat(Url, "&")
    end,
    {ParamName, ParamValue}=H,
    append_params_to_url(string:concat(string:concat(string:concat(NewUrlBase, escape_uri(ParamName)), "="), escape_uri(ParamValue)), T); 
append_params_to_url(Url, [])->
    Url.


append_params_to_body(Body, [H|T])->  
    case length(Body) of
	0 ->
	    NewBodyBase = [];
	_ ->
	    NewBodyBase = string:concat(Body, "&")
    end,
    {ParamName, ParamValue}=H,
    append_params_to_url(string:concat(string:concat(string:concat(NewBodyBase, escape_uri(ParamName)), "="), escape_uri(ParamValue)), T); 
append_params_to_body(Body, [])->
    Body.


    
   
chunk(BinaryMedia, ChunkSize)->    
    chunk(BinaryMedia, ChunkSize, []).
chunk(BinaryMedia, ChunkSize, Chunks)->
    case byte_size(BinaryMedia) < ChunkSize of 
	true ->
	    lists:append(Chunks, [base64:encode_to_string(BinaryMedia)]);
	false ->
	    {H,T}=split_binary(BinaryMedia, ChunkSize),
	    NewChunks=lists:append(Chunks, [base64:encode_to_string(H)]), 
	    chunk(T, ChunkSize, NewChunks)
    end.

generate_multipart__body(Params, Boundary)->
    generate_multipart__body(Params, Boundary, []).

generate_multipart__body([H|T], Boundary, Acc)->
    {Name, Value}=H,
    NewAcc=string:concat(Acc,string:concat(string:concat(string:concat(string:concat(string:concat(Boundary, "\nContent-Disposition: form-data; name=\""), Name),"\"\n\n"), Value), "\n")),
    generate_multipart__body(T, Boundary, NewAcc);
generate_multipart__body([], Boundary, Acc) ->
    string:concat(Acc, Boundary).

									
key_to_string(Key)->
    case is_atom(Key) of
	true ->
	    atom_to_list(Key);
	false  ->
	    case is_number(Key) of
		true ->
		    integer_to_list(Key);
		false  ->
		    Key
	    end
    end.
    
