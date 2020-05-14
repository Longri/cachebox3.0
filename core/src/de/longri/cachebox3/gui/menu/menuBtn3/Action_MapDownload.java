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
import de.longri.cachebox3.gui.widgets.menu.Menu;
import de.longri.cachebox3.gui.widgets.menu.MenuItem;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Language;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.CharSequenceUtil;
import de.longri.cachebox3.utils.http.ProgressCancelDownloader;
import de.longri.cachebox3.utils.http.Webb;
import de.longri.cachebox3.utils.http.ZipDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class Action_MapDownload extends AbstractAction {
    private static Logger log = LoggerFactory.getLogger(Action_MapDownload.class);
    private Menu icm;

    public Action_MapDownload() {
        super("MapDownload", MenuID.AID_Download_FZK_Map);
    }

    public static Array<MapRepositoryInfo> getMapInfoList(InputStream stream) {
        final Action_MapDownload.MapRepositoryInfo[] info = {new Action_MapDownload.MapRepositoryInfo()};
        final Array<Action_MapDownload.MapRepositoryInfo> list = new Array<>();

        final XmlStreamParser parser = new XmlStreamParser();


        parser.registerDataHandler("/Freizeitkarte/Map/Name", new XmlStreamParser.DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                info[0].Name = new String(data, offset, length).trim();
            }
        });

        if (Config.localisation.getEnumValue() == Language.de) {
            parser.registerDataHandler("/Freizeitkarte/Map/DescriptionGerman", new XmlStreamParser.DataHandler() {
                @Override
                protected void handleData(char[] data, int offset, int length) {
                    info[0].Description = new String(data, offset, length).trim();
                }
            });
        } else {
            parser.registerDataHandler("/Freizeitkarte/Map/DescriptionEnglish", new XmlStreamParser.DataHandler() {
                @Override
                protected void handleData(char[] data, int offset, int length) {
                    info[0].Description = new String(data, offset, length).trim();
                }
            });
        }

        parser.registerDataHandler("/Freizeitkarte/Map/Url", new XmlStreamParser.DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                info[0].Url = new String(data, offset, length).trim();
            }
        });

        parser.registerDataHandler("/Freizeitkarte/Map/Size", new XmlStreamParser.DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                info[0].Size = CharSequenceUtil.parseInteger(data, offset, length);
            }
        });

        parser.registerDataHandler("/Freizeitkarte/Map/Checksum", new XmlStreamParser.DataHandler() {
            @Override
            protected void handleData(char[] data, int offset, int length) {
                info[0].MD5 = new String(data, offset, length).trim();
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
            Array<MapRepositoryInfo> list = Action_MapDownload.getMapInfoList(inputStream);

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
                icm.addMenuItem("", mapInfo.Description.substring(14) + " (" + mapInfo.Size / 1024 / 1024 + " MB)",
                        null,
                        new ClickListener() {
                            public void clicked(InputEvent event, float x, float y) {
                                if (icm.mustHandle(event)) {
                                    MenuItem mi = (MenuItem) event.getListenerActor();
                                    mi.setEnabled(false);
                                    // todo doesn't show disabled (enough animation?)
                                    int slashPos = mapInfo.Url.lastIndexOf("/");
                                    String zipFile = mapInfo.Url.substring(slashPos + 1);
                                    String target = targetPath + "/" + zipFile;


                                    final ProgressCancelDownloader downloader = new ProgressCancelDownloader();

                                    //add downloader object
                                    try {
                                        downloader.add(new ZipDownloader(new URL(mapInfo.Url), Gdx.files.absolute(target)));
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
                                                log.warn("can't delete zip file from downloaded Map: {}", mapInfo.Description);
                                            else
                                                log.debug("delete zip file from downloaded Map: {}", mapInfo.Description);
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
        public String Name;
        public String Description;
        public String Url;
        public int Size;
        public String MD5;
    }

}
