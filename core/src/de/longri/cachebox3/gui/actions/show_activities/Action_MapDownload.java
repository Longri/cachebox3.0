package de.longri.cachebox3.gui.actions.show_activities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;
import com.thebuzzmedia.sjxp.rule.IRule.Type;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.UnZip;
import de.longri.cachebox3.utils.http.Download;
import de.longri.cachebox3.utils.http.Webb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import static de.longri.cachebox3.gui.menu.MenuID.AID_Download_FZK_Map;

public class Action_MapDownload extends AbstractAction {
    private static Logger log = LoggerFactory.getLogger(Action_MapDownload.class);
    private Array<MapRepositoryInfo> mapInfoList = new Array<>();
    private MapRepositoryInfo mapInfo;
    private Menu icm;

    public Action_MapDownload() {
        super("MapDownload", AID_Download_FZK_Map);
    }

    @Override
    public void execute() {
        try {
            if (mapInfoList.size == 0) {
                icm = new Menu("MapDownload");
                String repository_freizeitkarte_android = Webb.create()
                        .get("http://repository.freizeitkarte-osm.de/repository_freizeitkarte_android.xml")
                        .readTimeout(Config.socket_timeout.getValue())
                        .ensureSuccess()
                        .asString()
                        .getBody();
                Map<String, String> values = new HashMap<>();
                System.setProperty("sjxp.namespaces", "false");
                Array<IRule<Map<String, String>>> ruleList = createRepositoryRules(new Array<>());
                XMLParser<Map<String, String>> parserCache = new XMLParser<>(ruleList.toArray(IRule.class));
                parserCache.parse(new ByteArrayInputStream(repository_freizeitkarte_android.getBytes()), values);

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
                for (MapRepositoryInfo mapInfo : mapInfoList) {
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
                                        Download.Download(mapInfo.Url, target);
                                        try {
                                            UnZip unzip = new UnZip();
                                            unzip.extractFolder(target, true);
                                        } catch (Exception e) {
                                            log.error(e.getLocalizedMessage());
                                        }
                                        Gdx.files.absolute(target).delete();
                                    }
                                }
                            });
                }
            }

            icm.show();

        } catch (Exception ex) {
            MessageBox.show(ex.toString(), Translation.get("MapDownload"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
        }
    }

    @Override
    public Drawable getIcon() {
        return CB.getSkin().getMenuIcon.baseMapFreizeitkarte;
    }

    private Array<IRule<Map<String, String>>> createRepositoryRules(Array<IRule<Map<String, String>>> ruleList) {
        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/Name") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                mapInfo.Name = text;
            }
        });

        if (Config.localisation.getValue().equals("de")) {
            ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/DescriptionGerman") {
                @Override
                public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                    mapInfo.Description = text;
                }
            });
        } else {
            ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/DescriptionEnglish") {
                @Override
                public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                    mapInfo.Description = text;
                }
            });
        }

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/Url") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                mapInfo.Url = text;
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/Size") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                mapInfo.Size = Integer.parseInt(text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/Checksum") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                mapInfo.MD5 = text;
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/Freizeitkarte/Map") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    mapInfo = new MapRepositoryInfo();
                } else {
                    mapInfoList.add(mapInfo);
                }

            }
        });
        return ruleList;
    }

    public static class MapRepositoryInfo {
        public String Name;
        public String Description;
        public String Url;
        public int Size;
        public String MD5;
    }

}
