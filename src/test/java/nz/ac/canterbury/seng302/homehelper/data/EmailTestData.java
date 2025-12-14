package nz.ac.canterbury.seng302.homehelper.data;

import java.util.stream.Stream;

public class EmailTestData {
    public static Stream<String> validEmails() {
        return Stream.of(
                "jane@example.com",
                "john.doe@example.co.nz",
                "user123@example.co.uk",
                "u.ser+tag@example.com",
                "firstname-lastname@example.org",
                "user_name@example.io",
                "user@subdomain.example.com",
                "1234567890@example.com",
                "a@b.co",
                "user.name+tag+sorting@example.com",
                "x@example.com",
                "user'email@example.com",
                "user@hyphen-domain-example.com",
                "user@sub-domain.example.com"
        );
    }

    public static Stream<String> invalidEmails() {
        return Stream.of(
                "",
                "\n",
                "\t",
                " ",
                "    ",

                // incomplete
                "jane",
                "jane@",
                "@jane",
                "jane@com",
                "jane@.com",
                "jane@com.",
                "jane@.com.",
                "jane@com..com",
                "jane@-domain.com",
                "jane@domain-.com",

                // dots incorrectly used
                "jane..doe@example.com",
                "jane.....doe@example.com",
                ".jane@example.com",
                "jane.@example.com",
                "jane@.example.com",

                // unicode or illegal characters
                "jane@examp💥le.com",
                "jane@exam\nple.com",
                "jane@exam\tple.com",
                "jane@exam\rple.com",
                "jane@exam ple.com",
                "\"jane\"@example.com",
                "jane@\"example\".com",

                // trailing/leading spaces
                " jane@example.com ",
                "jane @example.com",
                "jane@ example.com",
                "jane@ example .com",

                // tld
                "jane@example.c",
                "jane@example.123",
                "jane@example.c-om",

                //no @ symbol
                "example",

                //no leading chars before @ symbol
                "@example.com",

                //illegal dash for email
                "-@example.com",

                //Illegal comma
                "jane@example,com",

                //Illegal numbers
                "jane@example.123.212",
                "jane@example.123"
        );
    }
}
