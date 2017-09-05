package seng302.visualiser.fxObjects;

import java.util.Arrays;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Extension of a ScrollPane that contains a TextFlow. Has an addMessage() function to parse and
 * display chatter text.
 */
public class ChatHistory extends ScrollPane {

    private TextFlow textFlow = new TextFlow();

    public ChatHistory() {
        this.setContent(textFlow);
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setMaxHeight(Double.MAX_VALUE);
        this.setMaxWidth(Double.MAX_VALUE);
        this.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        //This makes the window auto scroll.
        textFlow.getChildren().addListener((ListChangeListener<Node>) c ->
            this.setVvalue(1.0)
        );
        //This just makes it so that the ChatHistory is on focus it passes it off to the parent.
        this.parentProperty().addListener((obs, old, parent) ->
            this.focusedProperty().addListener((obsVal, oldVal, onFocus) -> {
                if (onFocus) {
                    parent.requestFocus();
                }
            })
        );
    }

    /**
     * Adds a message to chat history. Messages should be either of the form:
     * "[HH:MM:ss] \<player_name\>: \<message_text\>" or
     * "SERVER: \<message_text\>"
     * @param colour The colour of the user sending the message
     * @param Text The chatter text message to be displayed
     */
    public void addMessage (Paint colour, String Text) {
        String[] words = Text.split(":");
        if (words[0].trim().equals("SERVER")) {
            Text text = new Text(Text + "\n\n");
            text.setStyle("-fx-font-weight: bolder");
            textFlow.getChildren().add(text);
        } else {
            Text timePlayer = new Text(
                String.join(":", Arrays.copyOfRange(words, 0, 3)) + ":"
            );
            timePlayer.setStyle("-fx-font-weight: bold");
            timePlayer.setFill(colour);
            Text message = new Text(
                String.join(":", Arrays.copyOfRange(words, 3, words.length)) + "\n\n"
            );
            message.wrappingWidthProperty().bind(this.widthProperty());
            textFlow.getChildren().addAll(timePlayer, message);
        }

    }
}
