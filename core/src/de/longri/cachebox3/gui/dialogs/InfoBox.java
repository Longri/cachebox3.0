package de.longri.cachebox3.gui.dialogs;

import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.widgets.CB_ProgressBar;
import de.longri.cachebox3.gui.widgets.EditTextField;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.NamedRunnable;

public class InfoBox {
    private OnMsgBoxClickListener listener;
    private Infotype infoType;
    private ButtonDialog infoBox;

    private EditTextField edtInfo;
    private int maxVisibleLines;

    private CB_ProgressBar progressBar;

    private boolean isRunning, isCanceled;
    private String title;
    private String info;
    private MessageBoxButton buttons;

    public InfoBox(Infotype infoType, String title) {
        this.infoType = infoType;
        this.title = title;
        info = "";
        buttons = MessageBoxButton.Cancel;
        listener = (which, data) -> {
            // which can only be cancel (ButtonDialog.BUTTON_NEGATIVE)
            isCanceled = true;
            // the working activity will check isCanceled to finish itself and then call close
            // change Buttontext to "wait for finish"
            infoBox.setButtonText(Translation.get("waitForCancel"), ButtonDialog.BUTTON_NEGATIVE);
            infoBox.setButtonClickedListener(null);
            return false;
        };
        isCanceled = false;
    }

    private void create() {
        Catch_Table contentBox = new Catch_Table(true);
        switch (infoType) {
            case DISPLAY:
                maxVisibleLines = 10;
                edtInfo = new EditTextField(true);
                edtInfo.setEditable(false);
                edtInfo.setMinLineCount(maxVisibleLines);
                edtInfo.setWrap(true);
                contentBox.addLast(edtInfo);
                break;
                /*
            case INPUT:
                // not implemented yet
                break;
            case CHECKBOX:
                // not implemented yet
                break;
                 */
            case PROGRESS:
                progressBar = new CB_ProgressBar(0, 100, 1, false, "default");
                contentBox.addLast(progressBar);
                maxVisibleLines = 1;
                edtInfo = new EditTextField(false);
                edtInfo.setEditable(false);
                edtInfo.setMinLineCount(maxVisibleLines);
                edtInfo.setWrap(true);
                contentBox.addLast(edtInfo);
                // Drawable animationDrawable = VisUI.getSkin().getDrawable("download-animation");
                // workAnimation = new Image(animationDrawable);
                //dis.setAnimationType(AnimationType.Work);
                break;
        }
        infoBox = new ButtonDialog("InfoBox", contentBox, title, buttons, listener);
        infoBox.setNoHide();
    }

    public InfoBox open() {
        if (!isRunning) {
            create();
            isRunning = true;
            infoBox.show();
        }
        return this;
    }

    public void close() {
        if (isRunning) {
            isRunning = false;
            CB.postOnGlThread(new NamedRunnable("infoBox.hide()") {
                @Override
                public void run() {
                    infoBox.hide();
                }
            });
        }
    }

    public InfoBox setTitle(String title) {
        this.title = title;
        return this;
    }

    public InfoBox setInfo(CharSequence info) {
        return setInfo(info, true);
    }

    public InfoBox setInfo(CharSequence info, boolean clearPreviousInfo) {
        if (info != null && info.length() > 0) {
            if (clearPreviousInfo) this.info = "";
            // get the last lines, cause it is not possible to automatic/programmatically show only the end
            String[] old = this.info.split("\n");
            this.info += "\n" + info;
            int lineCount = old.length;
            StringBuilder out = new StringBuilder();
            if (lineCount > maxVisibleLines - 1) {
                for (int i = lineCount - maxVisibleLines + 1; i < lineCount; i++) {
                    out.append(old[i]).append("\n");
                }
                edtInfo.setText(out.toString() + info);
            } else {
                edtInfo.setText(this.info);
            }
        }
        return this;
    }

    public InfoBox setProgress(float percent, CharSequence text) {
        if (infoType == Infotype.PROGRESS)
            progressBar.setValue(percent);
        setInfo(text);
        return this;
    }

    public InfoBox setButtons(MessageBoxButton buttons) {
        this.buttons = buttons;
        return this;
    }

    /**
     * @return true, if the user has clicked cancel
     */
    public boolean isCanceled() {
        return isCanceled;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public enum Infotype {CHECKBOX, INPUT, DISPLAY, PROGRESS}

}
