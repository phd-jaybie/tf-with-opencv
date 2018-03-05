package org.tensorflow.demo.network;

import android.graphics.RectF;
import android.util.Xml;

import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.env.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by deg032 on 23/2/18.
 */

public class XmlOperator {

    private static final Logger LOGGER = new Logger();

    private static final String ns = null;

    public class XmlObject {
        private final String name;
        private final String description;

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        private XmlObject(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    public List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    public List parse(InputStream in, int height, int width) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser, height, width);
        } finally {
            in.close();
        }
    }

    private List readFeed(XmlPullParser parser, int height, int width) throws XmlPullParserException, IOException {
        List<Classifier.Recognition> entries = new ArrayList();

        Integer count = 0;
        parser.require(XmlPullParser.START_TAG, ns, "mr_objects");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("object")) {
                entries.add(readEntry(parser,count, height, width));
                count++;
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Classifier.Recognition> entries = new ArrayList();

        Integer count = 0;
        parser.require(XmlPullParser.START_TAG, ns, "mr_objects");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("object")) {
                //entries.add(readDetection(parser,count));
                count++;
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    private Classifier.Recognition readEntry(XmlPullParser parser, int id, int height, int width) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, ns, "object");
        String name;
        String title = null;
        float confidence = 0;
        float left = 0;
        float right = 0;
        float top = 0;
        float bottom = 0;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String parserName = parser.getName();
            if (parserName.equals("name")) {
                name = readName(parser);
                String[] parts = name.split(":",2);
                title = parts[0].replace("['","");
                String conf = parts[1].replace("%']","");
                confidence = (Float.parseFloat(conf))/100;
            } else if (parserName.equals("xmin")) {
                left = readNumber(parser,"xmin");
            } else if (parserName.equals("ymin")) {
                top = readNumber(parser, "ymin");
            } else if (parserName.equals("xmax")) {
                right = readNumber(parser, "xmax");
            } else if (parserName.equals("ymax")) {
                bottom = readNumber(parser, "ymax");
            } else {
                skip(parser);
            }
        }

        final RectF location =
                new RectF(left*width,top*height, right*width, bottom*height);
        return new Classifier.Recognition(""+id,title,confidence, location);
    }

    // Processes name tags in the feed.
    private String readName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "name");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "name");
        return title;
    }

    // Processes description tags in the feed.
    private String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "description");
        String summary = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "description");
        return summary;
    }

    private float readNumber(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, tag);
        float value = Float.parseFloat(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, tag);
        return value;
    }

    // For the tag name, extracts its text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}