package org.hccp.http.test;

import org.hccp.http.test.servlet.Header;

import java.util.*;

public class DiffTest {

    public static final String DIFF_TO_PREFIX = "&lt;&lt;";
    public static final String DIFF_FROM_PREFIX = "&gt;&gt;";

    List<Header> headers1;
    List<Header> headers2;

    String body1;
    String body2;


    public boolean isDiffable() {
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

    public String diffBody() {
        if (this.body1.equals(this.body2)) {
            return body1;
        } else {

            StringBuffer sb = new StringBuffer();

            String[] body1Parts = body1.split("\r\n");
            String[] body2Parts = body2.split("\r\n");

            String [] bigger = null;
            String [] smaller = null;
            int index = 0;

            String biggerPrefix = "";
            String smallerPrefix = "";

            boolean biggerFirst = false;

            if (body1Parts.length >= body2Parts.length) {
                bigger = body1Parts;
                smaller = body2Parts;
                biggerPrefix = DIFF_FROM_PREFIX;
                smallerPrefix = DIFF_TO_PREFIX;
                biggerFirst = true;
            } else {
                bigger = body2Parts;
                smaller = body1Parts;
                biggerPrefix = DIFF_TO_PREFIX;
                smallerPrefix = DIFF_FROM_PREFIX;
                biggerFirst = false;
            }

            for (int i = 0; i < smaller.length; i++) {
                String smallerValue = smaller[i];
                String biggerValue = bigger[i];

                if (biggerValue.equals(smallerValue)) {
                    sb.append(biggerValue + "\n");
                } else {
                    if (biggerFirst) {
                        sb.append(biggerPrefix + biggerValue + "\n");
                        sb.append(smallerPrefix + smallerValue + "\n");
                    } else {
                        sb.append(smallerPrefix + smallerValue + "\n");
                        sb.append(biggerPrefix + biggerValue + "\n");
                    }
                }
                index =i;
            }

            for (int i = index; i < bigger.length; i++) {
                String value = bigger[i];
                sb.append(biggerPrefix + value + "\n");
            }

            return sb.toString();

        }

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
                        sb.append(headerToDiffString("\t" + DIFF_FROM_PREFIX, "", values1));
                        sb.append(headerToDiffString("\t" + DIFF_TO_PREFIX, "", values2));

                    }


                } else if (headers1KeySet.contains(key)) {

                    prefix = DIFF_FROM_PREFIX;
                   sb.append(headerToDiffString(prefix, key, values1));

                } else {

                    prefix = DIFF_TO_PREFIX;
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

