package de.longri.cachebox3.gui.widgets;
/**
 *  copy of original
 */
public interface InputType {
    int TYPE_CLASS_DATETIME = 4;
    int TYPE_CLASS_NUMBER = 2;
    int TYPE_CLASS_PHONE = 3;
    int TYPE_CLASS_TEXT = 1;
    int TYPE_DATETIME_VARIATION_DATE = 16;
    int TYPE_DATETIME_VARIATION_NORMAL = 0;
    int TYPE_DATETIME_VARIATION_TIME = 32;
    int TYPE_MASK_CLASS = 15;
    int TYPE_MASK_FLAGS = 16773120;
    int TYPE_MASK_VARIATION = 4080;
    int TYPE_NULL = 0;
    int TYPE_NUMBER_FLAG_DECIMAL = 8192;
    int TYPE_NUMBER_FLAG_SIGNED = 4096;
    int TYPE_NUMBER_VARIATION_NORMAL = 0;
    int TYPE_NUMBER_VARIATION_PASSWORD = 16;
    int TYPE_TEXT_FLAG_AUTO_COMPLETE = 65536;
    int TYPE_TEXT_FLAG_AUTO_CORRECT = 32768;
    int TYPE_TEXT_FLAG_CAP_CHARACTERS = 4096;
    int TYPE_TEXT_FLAG_CAP_SENTENCES = 16384;
    int TYPE_TEXT_FLAG_CAP_WORDS = 8192;
    int TYPE_TEXT_FLAG_IME_MULTI_LINE = 262144;
    int TYPE_TEXT_FLAG_MULTI_LINE = 131072;
    int TYPE_TEXT_FLAG_NO_SUGGESTIONS = 524288;
    int TYPE_TEXT_VARIATION_EMAIL_ADDRESS = 32;
    int TYPE_TEXT_VARIATION_EMAIL_SUBJECT = 48;
    int TYPE_TEXT_VARIATION_FILTER = 176;
    int TYPE_TEXT_VARIATION_LONG_MESSAGE = 80;
    int TYPE_TEXT_VARIATION_NORMAL = 0;
    int TYPE_TEXT_VARIATION_PASSWORD = 128;
    int TYPE_TEXT_VARIATION_PERSON_NAME = 96;
    int TYPE_TEXT_VARIATION_PHONETIC = 192;
    int TYPE_TEXT_VARIATION_POSTAL_ADDRESS = 112;
    int TYPE_TEXT_VARIATION_SHORT_MESSAGE = 64;
    int TYPE_TEXT_VARIATION_URI = 16;
    int TYPE_TEXT_VARIATION_VISIBLE_PASSWORD = 144;
    int TYPE_TEXT_VARIATION_WEB_EDIT_TEXT = 160;
    int TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS = 208;
    int TYPE_TEXT_VARIATION_WEB_PASSWORD = 224;
}