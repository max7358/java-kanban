package com.yandex.app.http.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class LocalTimeTypeAdapter extends TypeAdapter<ZonedDateTime> {

    @Override
    public void write(final JsonWriter jsonWriter, final ZonedDateTime localDate) throws IOException {
        if (localDate == null) {
            jsonWriter.nullValue();
        } else
            jsonWriter.value(localDate.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
    }

    @Override
    public ZonedDateTime read(final JsonReader jsonReader) throws IOException {
        return ZonedDateTime.parse(jsonReader.nextString(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }
}