/*
 * Copyright (C) 2018 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.longri.cachebox3.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.*;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by Longri on 30.03.18.
 */
public abstract class XmlStreamEventParser {


    private final Array<QName> qNames = new Array<>();
    private final Array<QName> qNameHerachie = new Array<>();

    public XmlStreamEventParser() {

    }


    // see https://www.geeksforgeeks.org/stax-xml-parser-java/
    protected void parse(FileHandle xmlFile) throws FileNotFoundException, XMLStreamException {


        // Instance of the class which helps on reading tags
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);


        // Initializing the handler to access the tags in the XML file
        XMLEventReader eventReader =
                factory.createXMLEventReader(new FileReader(xmlFile.file()));

        // Checking the availabilty of the next tag
        while (eventReader.hasNext()) {
            // Event is actually the tag . It is of 3 types
            // <name> = StartEvent
            // </name> = EndEvent
            // data between the StartEvent and the EndEvent
            // which is Characters Event
            XMLEvent event = eventReader.nextEvent();

            // This will trigger when the tag is of type <...>
            if (event.isStartElement()) {
                StartElement element = (StartElement) event;
                this.startElement(element);
            }

            // This will be triggered when the tag is of type </...>
            if (event.isEndElement()) {
                EndElement element = (EndElement) event;
                this.endElement(element);
            }

            // Triggered when there is data after the tag which is
            // currently opened.
            if (event.isCharacters()) {
                // Depending upon the tag opened the data is retrieved .
                Characters element = (Characters) event;
                this.data(element);
            }
        }
    }

    public double parseDouble(Attribute attribute) {
        return Double.parseDouble(attribute.getValue());
    }

    public int parseInteger(Attribute attribute) {
        return Integer.parseInt(attribute.getValue());
    }

    public long parseLong(Attribute attribute) {
        return Long.parseLong(attribute.getValue());
    }

    public boolean parseBool(Attribute attribute) {
        return Boolean.parseBoolean(attribute.getValue());
    }


    protected ActiveQName registerName(String name) {
        ActiveQName qName = new ActiveQName(name);
        qNames.add(qName);
        return qName;
    }


    protected void startElement(StartElement element) {

        QName name = element.getName();
        qNameHerachie.add(name);
        // get registered
        ActiveQName registeredName = getRegisterdName(element.getName());
        if (registeredName != null) {
            registeredName.setActive();
            startElement(registeredName, element);
        }
    }


    protected void endElement(EndElement element) {

        QName name = qNameHerachie.pop();

        // get registered
        ActiveQName registeredName = getRegisterdName(name);
        if (registeredName != null) {
            endElement(registeredName, element);
            registeredName.setInActive();
        }
    }

    protected void data(Characters element) {
        QName name = qNameHerachie.peek();

        // get registered
        ActiveQName registeredName = getRegisterdName(name);
        if (registeredName != null) {
            data(registeredName, element);
        }

    }


    private ActiveQName getRegisterdName(QName name) {
        int idx = qNames.indexOf(name, false);
        if (idx >= 0) return (ActiveQName) qNames.get(idx);
        return null;
    }

    protected abstract void startElement(ActiveQName name, StartElement element);

    protected abstract void endElement(ActiveQName name, EndElement element);

    protected abstract void data(ActiveQName name, Characters element);
}