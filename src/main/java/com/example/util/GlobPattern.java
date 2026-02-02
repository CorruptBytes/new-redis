package com.example.util;

import java.util.regex.Pattern;

public final class GlobPattern {

    private GlobPattern() {}

    public static Pattern compile(String glob) {
        StringBuilder regex = new StringBuilder();
        regex.append("^");

        int i = 0;
        while (i < glob.length()) {
            char c = glob.charAt(i);

            switch (c) {
                case '*':
                    regex.append(".*");
                    break;
                case '?':
                    regex.append(".");
                    break;
                case '[':
                    int j = i + 1;
                    regex.append("[");
                    if (j < glob.length() && glob.charAt(j) == '!') {
                        regex.append("^");
                        j++;
                    }
                    while (j < glob.length() && glob.charAt(j) != ']') {
                        regex.append(glob.charAt(j++));
                    }
                    regex.append("]");
                    i = j; // 跳过 ]
                    break;
                default:
                    // regex 转义
                    if ("\\.[]{}()+-^$|".indexOf(c) >= 0) {
                        regex.append("\\");
                    }
                    regex.append(c);
            }
            i++;
        }

        regex.append("$");
        return Pattern.compile(regex.toString());
    }
}
