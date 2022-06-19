package org.hccp.http.test;

import org.hccp.http.test.servlet.Header;

import java.util.*;

public class DiffTest {

    public static final String DIFF2_PRFIX = "&lt;&lt;";
    List<Header> headers1;
    List<Header> headers2;

    String body1;
    String body2;


    public boolean isDiffable() {
        System.out.println("is diffable?");
        return (headers1 != null && headers2 != null && body1 != null && body2 != null);
    }

    public List<Header> getHeaders1() {
        return headers1;
    }

    public void setHeaders1(List<Header> headers1) {
        this.headers1 = headers1;
    }

    public List<Header> getHeaders2() {
        return headers2;
    }

    public void setHeaders2(List<Header> headers2) {
        this.headers2 = headers2;
    }

    public String getBody1() {
        return body1;
    }

    public void setBody1(String body1) {
        this.body1 = body1;
    }

    public String getBody2() {
        return body2;
    }

    public void setBody2(String body2) {
        this.body2 = body2;
    }


    public String diffHeaders() {

        StringBuffer sb = new StringBuffer();

        if (isDiffable()) {
            Set<String> headerKeys = new HashSet<String>();
            Iterator<Header> itr1 = headers1.iterator();
            while (itr1.hasNext()) {
                Header next = itr1.next();
                headerKeys.add(next.getName());
            }

            Iterator<Header> itr2 = headers2.iterator();
            while (itr2.hasNext()) {
                Header next = itr2.next();
                headerKeys.add(next.getName());
            }

            String[] keys = new String[headerKeys.size()];
            keys = headerKeys.toArray(keys);
            Arrays.sort(keys);

            Set<String> headers1KeySet = new HashSet<>();

            for (int j = 0; j < headers1.size(); j++) {
                headers1KeySet.add(headers1.get(j).getName());
            }

            Set<String> headers2KeySet = new HashSet<>();

            for (int j = 0; j < headers2.size(); j++) {
                headers2KeySet.add(headers2.get(j).getName());
            }


            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                String prefix = "";


                Header header1 = getHeaderForKey(key, getHeaders1());
                Header header2 = getHeaderForKey(key, getHeaders2());

                Set<String> values1 = header1.getValue();
                Set<String> values2 = header2.getValue();


                if (headers1KeySet.contains(key) && headers2KeySet.contains(key)) {



                    if (values1.containsAll(values2) && values2.containsAll(values1)) {
                        sb.append(headerToDiffString(prefix, key, values1));
                    } else {
                        sb.append(key + ":\n");
                        sb.append(headerToDiffString("\t>>", "", values1));
                        sb.append(headerToDiffString("\t" + DIFF2_PRFIX, "", values2));

                    }


                } else if (headers1KeySet.contains(key)) {

                    prefix = ">>";
                   sb.append(headerToDiffString(prefix, key, values1));

                } else {

                    prefix = DIFF2_PRFIX;
                    sb.append(headerToDiffString(prefix, key, values2));


                }

            }
        }
        return sb.toString();

    }

    private String headerToDiffString(String prefix, String key, Set<String> headerValues) {
        StringBuffer sb = new StringBuffer();
        sb.append(prefix + (key == "" ? "" : (key  + ": ")) );
        Iterator<String> valuesItr =  headerValues.iterator();
        while (valuesItr.hasNext()) {
            String value = valuesItr.next();
            sb.append(value);
            if (valuesItr.hasNext()) {
                sb.append(", ");
            }
        }
         sb.append("\n");
        return sb.toString();
    }
    private Header getHeaderForKey(String key, List<Header> headers) {
        for (int i = 0; i < headers.size(); i++) {
            Header header = headers.get(i);
            if (key.equals(header.getName())) {
                return header;
            }
        }
        return null;
    }
}

