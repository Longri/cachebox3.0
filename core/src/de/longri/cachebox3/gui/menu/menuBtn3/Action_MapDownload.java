package de.longri.cachebox3.gui.menu.menuBtn3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlStreamParser;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.callbacks.FinishCallBack;
import de.longri.cachebox3.gui.dialogs.CancelProgressDialog;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButton;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.menu.MenuID;
import de.longri.cachebox3.gui.stages.AbstractAction;
import de.longri.cachebox3.gui.views.MapView;
import de.longri.cachebox3.gui.widgets.menu.Menu;
import de.longri.cachebox3.gui.widgets.menu.MenuItem;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Language;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.CharSequenceUtil;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.http.ProgressCancelDownloader;
import de.longri.cachebox3.utils.http.Webb;
import de.longri.cachebox3.utils.http.ZipDownloader;
import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;

public class Action_MapDownload extends AbstractAction {
    private static Logger log = LoggerFactory.getLogger(Action_MapDownload.class);
    private Menu icm;

    public Action_MapDownload() {
        super("MapDownload", MenuID.AID_Download_FZK_Map);
    }

    public Array<MapRepositoryInfo> getMapInfoList(InputStream stream) {
        final Action_MapDownload.MapRepositoryInfo[] info = {new Action_MapDownload.MapRepositoryInfo()};
        final Array<Action_MapDownload.MapRepositoryInfo> list = new Array<>();

        final XmlStreamParser parser = new XmlStreamParser();


        parser.registerDataHandler("/Freizeitkarte/Map/Name", new XmlStreamParser.DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                info[0].name = new String(data, offset, length).trim();
            }
        });

        if (Config.localisation.getEnumValue() == Language.de) {
            parser.registerDataHandler("/Freizeitkarte/Map/DescriptionGerman", new XmlStreamParser.DataHandler() {
                @Override
                protected void handleData(char[] data, int offset, int length) {
                    info[0].description = new String(data, offset, length).trim();
                }
            });
        } else {
            parser.registerDataHandler("/Freizeitkarte/Map/DescriptionEnglish", new XmlStreamParser.DataHandler() {
                @Override
                protected void handleData(char[] data, int offset, int length) {
                    info[0].description = new String(data, offset, length).trim();
                }
            });
        }

        parser.registerDataHandler("/Freizeitkarte/Map/Url", new XmlStreamParser.DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                info[0].url = new String(data, offset, length).trim();
            }
        });

        parser.registerDataHandler("/Freizeitkarte/Map/Size", new XmlStreamParser.DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                info[0].size = CharSequenceUtil.parseInteger(data, offset, length);
            }
        });

        parser.registerDataHandler("/Freizeitkarte/Map/Checksum", new XmlStreamParser.DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                info[0].md5 = new String(data, offset, length).trim();
            }
        });

        parser.registerDataHandler("/Freizeitkarte/Map/MapsforgeBoundingBoxMinLat", new XmlStreamParser.DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                info[0].minLatE6 = CharSequenceUtil.parseInteger(data, offset, length);
            }
        });

        parser.registerDataHandler("/Freizeitkarte/Map/MapsforgeBoundingBoxMinLon", new XmlStreamParser.DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                info[0].minLonE6 = CharSequenceUtil.parseInteger(data, offset, length);
            }
        });

        parser.registerDataHandler("/Freizeitkarte/Map/MapsforgeBoundingBoxMaxLat", new XmlStreamParser.DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                info[0].maxLatE6 = CharSequenceUtil.parseInteger(data, offset, length);
            }
        });

        parser.registerDataHandler("/Freizeitkarte/Map/MapsforgeBoundingBoxMaxLon", new XmlStreamParser.DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                info[0].maxLonE6 = CharSequenceUtil.parseInteger(data, offset, length);
            }
        });

        parser.registerEndTagHandler("/Freizeitkarte/Map", new XmlStreamParser.EndTagHandler() {
            @Override
            protected void handleEndTag() {
                list.add(info[0]);
                info[0] = new Action_MapDownload.MapRepositoryInfo();
            }
        });

        try {
            parser.parse(stream);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        info[0] = null;
        return list;
    }

    @Override
    public void execute() {
        try {
            icm = new Menu("MapDownload");
            InputStream inputStream = Webb.create()
                    .get("http://repository.freizeitkarte-osm.de/repository_freizeitkarte_android.xml")
                    .readTimeout(Config.socket_timeout.getValue())
                    .ensureSuccess()
                    .asStream()
                    .getBody();
            Array<MapRepositoryInfo> list = getMapInfoList(inputStream);

            list.sort(new MapComparer(MapView.getLastCenterPos()));

            // get and check the target directory (global value)
            String workPath = Config.MapPackFolder.getValue();
            boolean isWritable;
            if (workPath.length() > 0)
                isWritable = Gdx.files.absolute(workPath).file().canWrite();
            else
                isWritable = false;
            if (isWritable)
                log.info("Download to " + workPath);
            else {
                log.error("Download to " + workPath + " is not possible!");
                // don't use Config.MapPackFolder.getDefaultValue()
                // because it doesn't reflect own repository
                // own or global repository is writable by default, but do check again
                workPath = Config.MapPackFolderLocal.getValue();
                isWritable = Gdx.files.absolute(workPath).file().canWrite();
                log.info("Download to " + workPath + " is possible? " + isWritable);
            }
            final String targetPath = workPath;
            // todo perhaps sorting mapInfoList
            // icm.setHideWithItemClick(true);

            for (MapRepositoryInfo mapInfo : list) {
                icm.addMenuItem("", mapInfo.description.substring(14) + " (" + mapInfo.size / 1024 / 1024 + " MB)",
                        null,
                        new ClickListener() {
                            public void clicked(InputEvent event, float x, float y) {
                                if (icm.mustHandle(event)) {
                                    MenuItem mi = (MenuItem) event.getListenerActor();
                                    mi.setEnabled(false);
                                    // todo doesn't show disabled (enough animation?)
                                    int slashPos = mapInfo.url.lastIndexOf("/");
                                    String zipFile = mapInfo.url.substring(slashPos + 1);
                                    String target = targetPath + "/" + zipFile;


                                    final ProgressCancelDownloader downloader = new ProgressCancelDownloader();

                                    //add downloader object
                                    try {
                                        downloader.add(new ZipDownloader(new URL(mapInfo.url), Gdx.files.absolute(target)));
                                    } catch (MalformedURLException e) {
                                        log.error("download", e);
                                    }


                                    // show dialog and start downloaderRunable
                                    // if cancel clicked or all downloads are ready, the CancelProgressDialog is closed automatically
                                    CancelProgressDialog cancelProgressDialog = new CancelProgressDialog("name", Translation.get("MapDownload"), downloader);
                                    cancelProgressDialog.show(new FinishCallBack() {
                                        @Override
                                        public void callBack() {
                                            //delete zip file after extraction
                                            if (!Gdx.files.absolute(target).delete())
                                                log.warn("can't delete zip file from downloaded Map: {}", mapInfo.description);
                                            else
                                                log.debug("delete zip file from downloaded Map: {}", mapInfo.description);
                                        }
                                    });
                                }
                            }
                        });
            }

            icm.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.show(ex.toString(), Translation.get("MapDownload"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
        }
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().menuIcon.baseMapFreizeitkarte;
    }

    public static class MapRepositoryInfo {
        public String name;
        public String description;
        public String url;
        public int size;
        public int minLatE6;
        public int minLonE6;
        public int maxLatE6;
        public int maxLonE6;
        String md5;
        BoundingBox bb = null;
        Coordinate center = null;
        Coordinate mi = null;
        Coordinate ma = null;

        public BoundingBox getBoundingBox() {
            if (bb == null) bb = new BoundingBox(minLatE6, minLonE6, maxLatE6, maxLonE6);
            return bb;
        }

        public Coordinate getCenter() {
            if (center == null) {
                GeoPoint cE6 = getBoundingBox().getCenterPoint();
                center = new Coordinate(cE6.getLatitude(), cE6.getLongitude());
            }
            return center;
        }

        public Coordinate getMin() {
            if (mi == null) {
                GeoPoint miE6 = new GeoPoint(minLatE6, minLonE6);
                mi = new Coordinate(miE6.getLatitude(), miE6.getLongitude());
            }
            return mi;
        }

        public Coordinate getMax() {
            if (ma == null) {
                GeoPoint maE6 = new GeoPoint(maxLatE6, maxLonE6);
                ma = new Coordinate(maE6.getLatitude(), maE6.getLongitude());
            }
            return ma;
        }
    }

    static class MapComparer implements Comparator<MapRepositoryInfo> {
        GeoPoint centreE6;
        Coordinate centre;

        public MapComparer(Coordinate centre) {
            this.centre = centre;
            centreE6 = new GeoPoint(centre.getLatitude(), centre.getLongitude());
        }

        @Override
        public int compare(MapRepositoryInfo a, MapRepositoryInfo b) {
            if ((a == null) || (b == null)) {
                return 0;
            } else {
                boolean aIsIn = a.getBoundingBox().contains(centreE6);
                boolean bIsIn = b.getBoundingBox().contains(centreE6);
                if (aIsIn && bIsIn) {
                    // vereinfachend in Relation zur BoundingBox Diagonalen vergleichen.
                    double ad = a.getCenter().distance(centre, MathUtils.CalculationType.FAST) / a.getMin().distance(a.getMax(), MathUtils.CalculationType.FAST);
                    double bd = b.getCenter().distance(centre, MathUtils.CalculationType.FAST) / b.getMin().distance(b.getMax(), MathUtils.CalculationType.FAST);
                    if (!a.description.startsWith("*")) a.description = "*" + a.description; // mark as containing mapcenter
                    if (!b.description.startsWith("*")) b.description = "*" + b.description; // mark as containing mapcenter
                    return (int) ((ad - bd) * 1000);
                } else {
                    if (aIsIn && !bIsIn) {
                        return -1;
                    } else if (!aIsIn && bIsIn) {
                        return 1;
                    }
                    // don't need this map
                    return 0;
                }
            }
        }
    }

}
