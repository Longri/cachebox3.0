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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.CB;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Longri on 21.02.2018.
 */
public class SensorIO {
    private boolean record = false;
    private Writer writer;
    private final char NEW_GPS_POS = 'G';
    private final char NEW_NETWORK_POS = 'N';
    private final char NEW_ALTITUDE = 'A';
    private final char NEW_BEARING_GPS = 'B';
    private final char NEW_BEARING_COMPASS = 'C';
    private final char NEW_SPEED = 'S';
    private final char NEW_ROLL = 'R';
    private final char NEW_PITCH = 'P';

    private AtomicInteger flushCount = new AtomicInteger(0);

    public void start() {
        Date now = new Date();
        DateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.getDefault());
        FileHandle fileHandle = Gdx.files.absolute(CB.WorkPath + "/user/temp/sensor" + dateFormatter.format(now) + ".lon");
        writer = fileHandle.writer(true);
        record = true;
    }

    public void stop() {
        record = false;
        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer = null;
    }

    public void write_newGpsPos(double latitude, double longitude, float accuracy) {
        if (!record || isPlay) return;
        try {
            writer.write(getTimeDiv());
            writer.write(NEW_GPS_POS + "#" + Double.toString(latitude));
            writer.write("#" + Double.toString(longitude) + "#" + Float.toString(accuracy) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkFlush();
    }

    public void write_newNetworkPos(double latitude, double longitude, float accuracy) {
        if (!record || isPlay) return;
        try {
            writer.write(getTimeDiv());
            writer.write(NEW_NETWORK_POS + "#" + Double.toString(latitude));
            writer.write("#" + Double.toString(longitude) + "#" + Float.toString(accuracy) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkFlush();
    }

    public void write_newAltitude(double altitude) {
        if (!record || isPlay) return;
        try {
            writer.write(getTimeDiv());
            writer.write(NEW_ALTITUDE + "#" + Double.toString(altitude) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkFlush();
    }

    public void write_newBearingCompass(float bearing) {
        if (!record || isPlay) return;
        try {
            writer.write(getTimeDiv());
            writer.write(NEW_BEARING_COMPASS + "#" + Float.toString(bearing) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkFlush();
    }

    public void write_newBearingGPS(float bearing) {
        if (!record || isPlay) return;
        try {
            writer.write(getTimeDiv());
            writer.write(NEW_BEARING_GPS + "#" + Float.toString(bearing) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkFlush();
    }

    public void write_newPitch(float pitch) {
        if (!record || isPlay) return;
        try {
            writer.write(getTimeDiv());
            writer.write(NEW_PITCH + "#" + Float.toString(pitch) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkFlush();
    }

    public void write_newRoll(float roll) {
        if (!record || isPlay) return;
        try {
            writer.write(getTimeDiv());
            writer.write(NEW_ROLL + "#" + Float.toString(roll) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkFlush();
    }

    public void write_newSpeed(double speed) {
        if (!record || isPlay) return;
        try {
            writer.write(getTimeDiv());
            writer.write(NEW_SPEED + "#" + Double.toString(speed) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkFlush();
    }

    private long lastTime = Long.MIN_VALUE;

    private String getTimeDiv() {
        String ret;
        if (lastTime == Long.MIN_VALUE) {
            ret = "0#";
            lastTime = System.currentTimeMillis();
        } else {
            long div = System.currentTimeMillis() - lastTime;
            lastTime = System.currentTimeMillis();
            ret = Long.toString(div) + "#";
        }
        return ret;
    }

    private void checkFlush() {
        int MAX_FLUSH = 100;
        if (flushCount.incrementAndGet() >= MAX_FLUSH) {
            flushCount.set(0);
            try {
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isRecord() {
        return record;
    }


    //#########################################################
    // Read and play simulation

    private boolean isPlay = false;

    public void stopPlay() {
        isPlay = false;
    }

    public void play(FileHandle file) {
        String fullText = file.readString();
        final String[] lines = fullText.split("\n");
        isPlay = true;
        CB.postAsync(new NamedRunnable("play sensor simulation") {
            @Override
            public void run() {
                for (String line : lines) {

                    if (!isPlay) {
                        break;
                    }

                    String[] lineSplites = line.split("#");

                    // wait to next event
                    int waitTime = Integer.parseInt(lineSplites[0]);
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    char command = lineSplites[1].charAt(0);

//                    switch (command) {
//                        case NEW_GPS_POS:
//                            CB.eventHelper.newGpsPos(Double.parseDouble(lineSplites[2]),
//                                    Double.parseDouble(lineSplites[3]), Float.parseFloat(lineSplites[4]));
//                            break;
//                        case NEW_NETWORK_POS:
//                            CB.eventHelper.newNetworkPos(Double.parseDouble(lineSplites[2]),
//                                    Double.parseDouble(lineSplites[3]), Float.parseFloat(lineSplites[4]));
//                            break;
//                        case NEW_ALTITUDE:
//                            CB.eventHelper.newAltitude(Double.parseDouble(lineSplites[2]));
//                            break;
//                        case NEW_BEARING_GPS:
//                            CB.eventHelper.newBearing(Float.parseFloat(lineSplites[2]), true);
//                            break;
//                        case NEW_BEARING_COMPASS:
//                            CB.eventHelper.newBearing(Float.parseFloat(lineSplites[2]), false);
//                            break;
//                        case NEW_SPEED:
//                            CB.eventHelper.newSpeed(Double.parseDouble(lineSplites[2]));
//                            break;
//                        case NEW_PITCH:
//                            CB.eventHelper.newPitch(Float.parseFloat(lineSplites[2]));
//                            break;
//                        case NEW_ROLL:
//                            CB.eventHelper.newRoll(Float.parseFloat(lineSplites[2]));
//                            break;
//                    }
                }
                isPlay = false;
            }
        });

    }

    public boolean isPlay() {
        return isPlay;
    }

}
