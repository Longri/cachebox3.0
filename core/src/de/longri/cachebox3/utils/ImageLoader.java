/*
 * Copyright (C) 2015-2017 team-cachebox.de
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
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.settings.Settings;
import de.longri.cachebox3.utils.texturepacker.TexturePacker_Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author Longri
 */
public class ImageLoader {

    final static Logger log = LoggerFactory.getLogger(ImageLoader.class);

    private final boolean thumbnail;
    public boolean ImageLoadError = false;
    private int State = 0;
    private Thread loadingThread;
    private String mPath;
    private Texture mImageTex = null;
    private Drawable mDrawable = null;
    private Thread ImageDownloadThread;
    private boolean isPacking = false;
    private HashMap<String, TextureAtlas> Atlanten;
    private String AtlasPath;
    private String ImgName;
    private boolean inGenerate = false;
    private float frameCounter = 0;
    private SpriteDrawable animDrawable;
    private Sprite animSprite;

    private Animation<TextureRegion> anim;

    public boolean inLoad = false;
    private float spriteWidth;
    private float spriteHeight;
    public boolean reziseHeight;

    public ImageLoader() {
        thumbnail = false;
    }

    public ImageLoader(boolean thumb) {
        thumbnail = thumb;
    }

    public interface resize {
        public void sizechanged(float width, float height);
    }

    private resize resizeListener;
    private float resizeWidth = 0;

    public void setResizeListener(resize listener, float width) {
        resizeListener = listener;
        resizeWidth = width;
    }

    private String ThumbPräfix = "";

    public void setThumbWidth(float width, String präfix) {
        resizeWidth = width;
        ThumbPräfix = präfix;
    }

    public resize getResizeListener() {
        return resizeListener;
    }

    /**
     * Pack the images from Folder into a Atlas and Load the Image from Atlas
     */
    private void packImagesToTextureAtlas(String ImagePath, boolean reziseHeight) {
        CB.assertGlThread();

        if (isPacking)
            return;
        isPacking = true;

        de.longri.cachebox3.utils.texturepacker.Settings textureSettings = new de.longri.cachebox3.utils.texturepacker.Settings();

        textureSettings.pot = true;
        textureSettings.paddingX = 2;
        textureSettings.paddingY = 2;
        textureSettings.duplicatePadding = true;
        textureSettings.edgePadding = true;
        textureSettings.rotation = false;
        textureSettings.minWidth = 16;
        textureSettings.minHeight = 16;
        textureSettings.maxWidth = 2048;
        textureSettings.maxHeight = 2048;
        textureSettings.stripWhitespaceX = false;
        textureSettings.stripWhitespaceY = false;
        textureSettings.alphaThreshold = 0;
        textureSettings.filterMin = TextureFilter.Nearest;
        textureSettings.filterMag = TextureFilter.Nearest;
        textureSettings.wrapX = TextureWrap.ClampToEdge;
        textureSettings.wrapY = TextureWrap.ClampToEdge;
        textureSettings.format = Format.RGBA8888;
        textureSettings.alias = true;
        textureSettings.outputFormat = "png";
        textureSettings.jpegQuality = 0.9f;
        textureSettings.ignoreBlankImages = true;
        textureSettings.fast = false;
        textureSettings.debug = false;

        String inputFolder = Utils.getDirectoryName(ImagePath);
        String outputFolder = Settings.ImageCacheFolder.getValue();
        String Name = getCachedAtlasName(inputFolder);

        try {
            TexturePacker_Base.process(textureSettings, inputFolder, outputFolder, Name);
        } catch (Exception e) {
            e.printStackTrace();
            ImageLoadError = true;
        }

        Sprite spt = tryToLoadFromCreatedAtlas(ImagePath);
        if (spt != null)
            setSprite(spt, reziseHeight);

        isPacking = false;
    }

    private String getCachedAtlasName(String inputFolder) {
        String Name = inputFolder.replace("/", "_");
        Name = Name.replace("\\", "_");
        Name = Name.replace(".", "");
        Name = Name + ".spp";
        return Name;
    }

    private Sprite tryToLoadFromCreatedAtlas(final String ImagePath) {
        AtomicBoolean WAIT = new AtomicBoolean(true);
        final Sprite[] tmp = {null};

        CB.postOnGlThread(new NamedRunnable("ImageLoader") {
            @Override
            public void run() {
                if (Atlanten == null)
                    Atlanten = new HashMap<String, TextureAtlas>();

                String inputFolder = Utils.getDirectoryName(ImagePath);
                String ImageName = Utils.getFileNameWithoutExtension(ImagePath);
                String Name = getCachedAtlasName(inputFolder);

                final String AtlasPath = Settings.ImageCacheFolder.getValue() + "/" + Name;
                if (!Utils.fileExistsNotEmpty(AtlasPath)) {
                    tmp[0] = null;
                    WAIT.set(false);
                }
                TextureAtlas atlas = null;
                if (Atlanten.containsKey(AtlasPath)) {
                    atlas = Atlanten.get(AtlasPath);
                } else {
                    ImageLoader.this.AtlasPath = AtlasPath;
                    ImageLoader.this.ImgName = ImageName;
                    State = 6;
                }


                if (atlas != null) {
                    tmp[0] = atlas.createSprite(ImageName);
                }
            }
        });

        while (WAIT.get()) {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return tmp[0];
    }

    void setAtlas(String atlasPath, String imgName, boolean reziseHeight) {
        CB.assertGlThread();
        State = 7;
        TextureAtlas atlas = new TextureAtlas(Gdx.files.absolute(atlasPath));
        Atlanten.put(atlasPath, atlas);

        Sprite tmp = null;
        if (atlas != null) {
            tmp = atlas.createSprite(imgName);
        }

        if (tmp != null)
            setSprite(tmp, reziseHeight);
    }

    void ThreadLoad(final boolean reziseHeight) {
        State = 5;
        if (isPacking)
            return;

        if (loadingThread != null) {
            if (loadingThread.getState() != Thread.State.TERMINATED)
                return;
            else
                loadingThread = null;
        }

        loadingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                Sprite spt = tryToLoadFromCreatedAtlas(mPath);
                if (spt != null) {
                    setSprite(spt, reziseHeight);
                } else {
                    packImagesToTextureAtlas(mPath, reziseHeight);
                }

            }
        });

        if (loadingThread.getState() == Thread.State.NEW)
            loadingThread.start();
    }

    public void setDrawable(Drawable drawable) {
        mDrawable = drawable;
        inLoad = false;
    }

    /**
     * Sets a Image URl and Downlowd this Image if this don't exist on Cache
     *
     * @param iconUrl
     */
    public void setImageURL(final String iconUrl) {
        if (iconUrl == null)
            return;
        if (iconUrl.length() == 0)
            return;

        if (ImageDownloadThread != null) {
            if (ImageDownloadThread.getState() != Thread.State.TERMINATED)
                return;
            else
                ImageDownloadThread = null;
        }

        ImageDownloadThread = new Thread(new Runnable() {

            @Override
            public void run() {
                final String CachePath = new File(Settings.ImageCacheFolder.getValue()).getAbsolutePath();

                // Search first slash after Http or www
                int slashPos = -1;
                slashPos = iconUrl.indexOf("http");
                if (slashPos == -1)
                    slashPos = iconUrl.indexOf("www");
                if (slashPos == -1)
                    slashPos = iconUrl.indexOf("file");
                if (slashPos == -1) {
                    ImageLoadError = true;
                    return; // invalid URL
                }
                slashPos += 7;
                slashPos = iconUrl.indexOf("/", slashPos);

                final String LocalPath = iconUrl.substring(slashPos);

                // check if Image exist on Cache
                if (Utils.fileExistsNotEmpty(CachePath + LocalPath)) {
                    setImage(CachePath + LocalPath);
                    return;
                }

                inLoad = true;

                // Download Image to Cache
                try {
                    URL url = new URL(iconUrl);

                    final Downloader dl = new Downloader(url, new File(CachePath + LocalPath));

                    Thread DLThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            dl.run();
                            inLoad = false;

                            // chk if Download complied
                            if (!Utils.fileExistsNotEmpty(CachePath + LocalPath)) {
                                // Download Error
                                ImageLoadError = true;
                                return;
                            }

                            setImage(CachePath + LocalPath);
                        }
                    });

                    DLThread.run();
                } catch (MalformedURLException e) {
                    log.error("ImageDownloader wrong URL: " + iconUrl, e);
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ImageDownloadThread.start();
        generate();
    }

    public void clearImage() {
        mDrawable = null;
        mPath = null;
    }

    public void setImage(String Path) {
        State = 3;
        mPath = Path.replace("file://", "");
        if (getDrawable(0) != null) {
            dispose();
            // das laden des Images in das Sprite darf erst in der Render Methode passieren, damit es aus dem GL_Thread herraus l�uft.
        }
        generate();
    }

    public void setSprite(Sprite sprite, boolean reziseHeight) {

        State = 7;
        inLoad = false;
        spriteWidth = sprite.getWidth();
        spriteHeight = sprite.getHeight();

        if (this.resizeListener != null) {
            float proportionWidth = resizeWidth / spriteWidth;
            if (proportionWidth > CB.getScalefactor()) {
                proportionWidth = CB.getScalefactor();
            }

            float newWidth = spriteWidth * proportionWidth;//* UI_Size_Base.that.getScale();
            float newHeight = spriteHeight * proportionWidth;//* UI_Size_Base.that.getScale();
            sprite.scale(proportionWidth);
            this.resizeListener.sizechanged(newWidth, newHeight);

        }

        mDrawable = new SpriteDrawable(sprite);
    }

    private void generate() {
        inLoad = true;
        if (ImageLoadError) {
            //Fixme use style
            setSprite(CB.getSkin().getSprite("deleteIcon"), this.reziseHeight);
            ImageLoadError = false;
            return;
        }

        if (State == 3) {
            if (inGenerate)
                return;
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    try {
                        inGenerate = true;

                        if (mPath.endsWith(".gif")) {
                            anim = GifDecoder.loadGIFAnimation(PlayMode.LOOP, Gdx.files.absolute(mPath).read());
                        } else {
                            loadAsync();
                        }

                    } catch (com.badlogic.gdx.utils.GdxRuntimeException e) {
                        ImageLoadError = true;
                        log.error("Load GL Image", e);
                        State = 4;
                    } catch (Exception e) {
                        ImageLoadError = true;
                        log.error("Load GL Image", e);
                        e.printStackTrace();
                    }
                    inGenerate = false;
                }
            });

            return;
        }

        if (State == 4)
            ThreadLoad(reziseHeight);

        if (State == 6)
            setAtlas(this.AtlasPath, this.ImgName, reziseHeight);
    }

    private static AssetManager assetManager = new AssetManager();

    private void loadAsync() {

        Thread th = new Thread(new Runnable() {

            @Override
            public void run() {

                if (thumbnail)
                    createThumb();
                final TextureLoader tl = new TextureLoader(new AbsoluteFileHandleResolver());
                try {
                    tl.loadAsync(assetManager, ImgName, Gdx.files.absolute(mPath), null);
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            // Log.info(log, "LoadSync " + mPath + ":" + ImgName);
                            mImageTex = tl.loadSync(assetManager, ImgName, Gdx.files.absolute(mPath), null);
                            Sprite sprite = new Sprite(mImageTex);
                            spriteWidth = sprite.getWidth();
                            spriteHeight = sprite.getHeight();
                            setSprite(sprite, reziseHeight);
                            // Log.info(log, "LoadSync " + mPath + ":" + ImgName + " ready");
                        }
                    });
                } catch (Exception e) {
                    //e.printStackTrace();
                }

            }
        });
        th.start();
    }

    private String originalPath = null;

    private void createThumb() {
        String tmp = PlatformConnector.createThumb(mPath, (int) resizeWidth, ThumbPräfix);
        if (tmp != null) {
            originalPath = mPath;
            mPath = tmp;
        } else {
            log.error("Thumb not generated for " + mPath + " ! " + ThumbPräfix);
        }
    }

    /**
     * Returns the drawable was is loaded.
     * <p>
     * If the loaded image a Animation, like GIF, so returns the drawable for the given KeyFrame.
     *
     * @param keyFrame
     * @return
     */
    public Drawable getDrawable(float keyFrame) {
        if (anim != null) {
            frameCounter += keyFrame;
            TextureRegion tex = anim.getKeyFrame(frameCounter);

            if (animSprite == null) {
                animSprite = new Sprite(tex);
                spriteWidth = animSprite.getWidth();
                spriteHeight = animSprite.getHeight();

                if (this.resizeListener != null) {
                    float proportionWidth = resizeWidth / spriteWidth;
                    if (proportionWidth > CB.getScalefactor()) {
                        proportionWidth = CB.getScalefactor();
                    }

                    float newWidth = spriteWidth * proportionWidth;
                    float newHeight = spriteHeight * proportionWidth;
                    animSprite.scale(proportionWidth);
                    this.resizeListener.sizechanged(newWidth, newHeight);

                }

            } else {
                animSprite.setRegion(tex);
            }

            if (animDrawable == null) {
                animDrawable = new SpriteDrawable(animSprite);
            } else {
                animDrawable.setSprite(animSprite);
            }

            return animDrawable;
        }
        return mDrawable;
    }

    public boolean isDrawableNULL() {
        if (mDrawable == null && anim == null)
            return true;
        return false;
    }

    public int getAnimDelay() {
        if (anim != null) {
            return (int) (anim.getFrameDuration() * 1000);
        }
        return 0;
    }

    public void dispose() {

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (mImageTex != null) {
                    try {
                        assetManager.unload(ImgName);
                    } catch (Exception e) {
                    }
                    mImageTex.dispose();
                }

                mImageTex = null;
                mDrawable = null;
                loadingThread = null;
                mPath = null;

                ImageDownloadThread = null;
                if (Atlanten != null) {
                    Atlanten.clear();
                    Atlanten = null;
                }

                AtlasPath = null;
                ImgName = null;
                animDrawable = null;

                animSprite = null;
                anim = null;
            }
        });
    }

    public float getSpriteWidth() {
        return spriteWidth;
    }

    public void setSpriteWidth(float spriteWidth) {
        this.spriteWidth = spriteWidth;
    }

    public float getSpriteHeight() {
        return spriteHeight;
    }

    public void setSpriteHeight(float spriteHeight) {
        this.spriteHeight = spriteHeight;
    }

    public String getImagePath() {
        return mPath;
    }

    public String getOriginalImagePath() {
        if (originalPath == null)
            return mPath;
        return originalPath;
    }

}