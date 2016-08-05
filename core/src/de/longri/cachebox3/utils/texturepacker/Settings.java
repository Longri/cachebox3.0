package de.longri.cachebox3.utils.texturepacker;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

/** @author Nathan Sweet */
public class Settings {
	public boolean pot = true;
	public int paddingX = 2, paddingY = 2;
	public boolean edgePadding = true;
	public boolean duplicatePadding = true;
	public boolean rotation;
	public int minWidth = 16, minHeight = 16;
	public int maxWidth = 1024, maxHeight = 1024;
	public boolean forceSquareOutput = false;
	public boolean stripWhitespaceX, stripWhitespaceY;
	public int alphaThreshold;
	public TextureFilter filterMin = TextureFilter.Nearest, filterMag = TextureFilter.Nearest;
	public TextureWrap wrapX = TextureWrap.ClampToEdge, wrapY = TextureWrap.ClampToEdge;
	public Format format = Format.RGBA8888;
	public boolean alias = true;
	public String outputFormat = "png";
	public float jpegQuality = 0.9f;
	public boolean ignoreBlankImages = true;
	public boolean fast;
	public boolean debug;
	public boolean combineSubdirectories;
	public boolean jsonOutput = true;
	public boolean flattenPaths;

	public Settings() {
	}

	public Settings(Settings settings) {
		fast = settings.fast;
		rotation = settings.rotation;
		pot = settings.pot;
		minWidth = settings.minWidth;
		minHeight = settings.minHeight;
		maxWidth = settings.maxWidth;
		maxHeight = settings.maxHeight;
		paddingX = settings.paddingX;
		paddingY = settings.paddingY;
		edgePadding = settings.edgePadding;
		alphaThreshold = settings.alphaThreshold;
		ignoreBlankImages = settings.ignoreBlankImages;
		stripWhitespaceX = settings.stripWhitespaceX;
		stripWhitespaceY = settings.stripWhitespaceY;
		alias = settings.alias;
		format = settings.format;
		jpegQuality = settings.jpegQuality;
		outputFormat = settings.outputFormat;
		filterMin = settings.filterMin;
		filterMag = settings.filterMag;
		wrapX = settings.wrapX;
		wrapY = settings.wrapY;
		duplicatePadding = settings.duplicatePadding;
		debug = settings.debug;
		combineSubdirectories = settings.combineSubdirectories;
		jsonOutput = settings.jsonOutput;
		flattenPaths = settings.flattenPaths;
	}
}