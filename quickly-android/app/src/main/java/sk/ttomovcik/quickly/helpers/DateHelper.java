package sk.ttomovcik.quickly.helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class DateHelper
{
    // Like date date. The time stuff. Sorry.

    public long getDate(String day) throws ParseException
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd/MM/yyyy", Locale.getDefault());
        Date date;
        date = dateFormat.parse(day);
        return Objects.requireNonNull(date).getTime();
    }
}
