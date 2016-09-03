package de.longri.cachebox3.gui.stages;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import org.slf4j.LoggerFactory;

/**
 * Created by Hoepfner on 25.08.2016.
 */
public class StageManager {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(StageManager.class);
    final static Array<NamedStage> stageList = new Array<NamedStage>(5);
    final static NamedStage toastStage = new NamedStage("toastStage");

    private static boolean debug = true;
    private static boolean writeDrawSequence = debug;

    private static NamedStage mainStage;
    private static InputMultiplexer inputMultiplexer;

    public static void draw() {

        if (stageList.size < 2) {
            mainStage.act();
            mainStage.draw();
            if (writeDrawSequence) log.debug("draw mainStage");
        }

//        //draw the last two stages
//        if (stageList.size <= 0) {
//            if (writeDrawSequence) log.debug("END stage drawing");
//            writeDrawSequence = false;
//            return;
//        }else{
//
//        }

        if (stageList.size == 1) {
            Stage stage = stageList.get(0);
            stage.act();
            stage.draw();
            if (writeDrawSequence) log.debug("draw Stage level 0");
        } else if (stageList.size >= 1) {
            for (int idx = stageList.size - 1; idx < stageList.size; idx++) {
                Stage stage = stageList.get(idx);
                stage.act();
                stage.draw();
                if (writeDrawSequence) log.debug("draw Stage level " + idx);
            }
        }

        // draw toastStage at last over all
        if (toastStage.getActors().size > 0) {
            toastStage.act();
            toastStage.draw();
            if (writeDrawSequence) log.debug("draw Toast Stage");
        }
        if (writeDrawSequence) log.debug("END stage drawing");
        writeDrawSequence = false;

    }

    public static void addToastActor(Actor actor) {
        toastStage.addActor(actor);
        if (debug) writeDrawSequence = true;
    }

    public static void showOnNewStage(final Actor actor) {


        if (stageList.size > 0) {
            String lastName = stageList.get(stageList.size - 1).getName();
            if (lastName.equals(actor.getName())) {

                // don't show double
                return;
            }
        }


        NamedStage newStage = new NamedStage(actor.getName());
        newStage.addActor(actor);
        newStage.setKeyboardFocus(actor);
        newStage.setScrollFocus(actor);


        stageList.add(newStage);
        log.debug("Add new Stage: " + newStage.getName());
        log.debug("Stage list: " + stageList.toString());

        //switch input processor to window stage

        if (stageList.size > 1) {
            inputMultiplexer.removeProcessor(stageList.get(stageList.size - 2));
        } else {
            inputMultiplexer.removeProcessor(mainStage);
        }


        addNonDoubleInputProzessor(newStage);

        log.debug("InputProzessors:" + inputMultiplexer.getProcessors().toString());

        if (debug) writeDrawSequence = true;
    }

    public static void showOnActStage(Actor actor) {
        Stage stage = stageList.get(stageList.size - 1);
        stage.addActor(actor);
    }

    public static void removeAllWithActStage() {
        NamedStage stage = stageList.pop();

        log.debug("Remove Stage: " + stage.getName());
        log.debug("Stage list: " + stageList.toString());

        //switch input processor to main stage
        inputMultiplexer.removeProcessor(stage);
        if (stageList.size > 0) {
            addNonDoubleInputProzessor(stageList.get(stageList.size - 1));
        } else {
            addNonDoubleInputProzessor(mainStage);
        }
        log.debug("InputProzessors:" + inputMultiplexer.getProcessors().toString());
        if (debug) writeDrawSequence = true;
    }

    public static void setMainStage(NamedStage stage) {
        mainStage = stage;

        // add mainStage to input processor
        if (inputMultiplexer != null) addNonDoubleInputProzessor(mainStage);
    }

    private static void addNonDoubleInputProzessor(InputProcessor processor) {
        if (inputMultiplexer.getProcessors().contains(processor, true)) return;
        inputMultiplexer.addProcessor(processor);
    }

    public static void setInputMultiplexer(InputMultiplexer newInputMultiplexer) {
        inputMultiplexer = newInputMultiplexer;
    }

    public static Batch getBatch() {
        return mainStage.getBatch();
    }
}
