package com.iyr.ian.utils.chat.models;

import com.google.firebase.database.Exclude;
import com.iyr.ian.dao.models.SpeedMessage;
import com.iyr.ian.dao.models.SpeedMessageActions;
import com.iyr.ian.utils.chat.enums.MessagesStatus;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

/*
 * Created by troy379 on 04.04.17.
 */


public class Message implements
        MessageContentType.Image, /*this is for default image messages implementation*/
        MessageContentType,
        Serializable/*and this one is for custom content type (in this case - voice message)*/ {

    //  private  SpeedMessage action;
    private String id;
    private String text = "";
    private Date createdAt;
    private Author author;

    private Image image;
    private Voice voice;
    private Video video;

    @Exclude
    private MessagesStatus status;


    public SpeedMessage action;


    public Message(String messageKey, Author user) {

        this(messageKey, user, new Date());
    }

    public Message(String messageKey, Author user, String text) {
        this(messageKey, user, text, new Date());
    }

    public Message(String messageKey, Author user, @NotNull SpeedMessage speedMessage) {
        this(messageKey, user, speedMessage, new Date());
    }


    public Message(String messageKey, Author author, @NotNull SpeedMessage speedMessage, Date createdAt) {
        this.id = messageKey;
        // this.action = speedMessage;
        this.author = author;
        this.createdAt = createdAt;
    }


    public Message(String messageKey, Author author, String text, Date createdAt) {
        this.id = messageKey;
        this.text = text;
        this.author = author;
        this.createdAt = createdAt;
    }

    public Message(String messageKey, Author author, Date createdAt) {
        this.id = messageKey;

        this.author = author;
        this.createdAt = createdAt;
    }

    public Message(@NotNull HashMap<String, Object> hashMap) {
        super();

        HashMap authorSection = (HashMap) hashMap.get("author");
        String userId = Objects.requireNonNull(authorSection).get("id").toString();
        String name = Objects.requireNonNull(authorSection.get("name")).toString();
        String avatar = authorSection.get("avatar").toString();
        Author author = new Author(userId, name, avatar, false);

        this.setId(Objects.requireNonNull(hashMap.get("id")).toString());

        this.setAuthor(author);
        if (hashMap.containsKey("text")) {
            this.setText(hashMap.get("text").toString());
        }
        long createTime = Long.parseLong(hashMap.get("createdAt").toString());
        this.setCreatedAt(new Date(createTime));

        if (hashMap.containsKey("image")) {
            String imageUrl = ((HashMap<String, String>) Objects.requireNonNull(hashMap.get("image"))).get("url");
            this.image = new Image(imageUrl);
        }

        if (hashMap.containsKey("action")) {
            HashMap actionMap = (HashMap) hashMap.get("action");
            var action = new SpeedMessage((String) actionMap.get("messageTag"),
                    SpeedMessageActions.valueOf((String) actionMap.get("actionType")),
                    Integer.valueOf(String.valueOf(actionMap.get("actionTitleResId"))),
                    Integer.valueOf(String.valueOf(actionMap.get("actionMessageResId"))),
                    SpeedMessageActions.valueOf((String) actionMap.get("revertActionType")),
                    Integer.valueOf(String.valueOf(actionMap.get("revertActionTitleResId"))),
                    Integer.valueOf(String.valueOf(actionMap.get("revertActionMessageResId")))
            );


            this.action = action;
        }

        if (hashMap.containsKey("voice")) {
            HashMap<String, Object> map = (HashMap<String, Object>) hashMap.get("voice");
            String voiceUrl = Objects.requireNonNull(map.get("url")).toString();
            int duration = Integer.valueOf(Objects.requireNonNull(map.get("duration")).toString());
            this.voice = new Voice(voiceUrl, duration);
            //this.voice.url = ((HashMap<String, String>) hashMap.get("voice")).get("url");
        }

        if (hashMap.containsKey("video")) {
            HashMap<String, Object> map = (HashMap<String, Object>) hashMap.get("video");
            String videoUrl = Objects.requireNonNull(map.get("url")).toString();
            int duration = Integer.valueOf(Objects.requireNonNull(map.get("duration")).toString());
            int width = Integer.valueOf(map.get("width").toString());
            int height = Integer.valueOf(Objects.requireNonNull(map.get("height")).toString());
            this.video = new Video(videoUrl, duration, width, height);
            //this.voice.url = ((HashMap<String, String>) hashMap.get("voice")).get("url");
        }
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(@Nullable String key) {
        this.id = key;
    }

    @Override
    public String getText() {
        return text;
    }


    public void setText(String text) {
        this.text = text;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public Author getUser() {
        return this.author;
    }

    private void setAuthor(Author user) {
        this.author = user;
    }

    @Override
    public String getImageUrl() {
        return image == null ? null : image.url;
    }

    public Voice getVoice() {
        return voice;
    }

    public void setVoice(Voice voice) {
        this.voice = voice;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public MessagesStatus getStatus() {
        return this.status;
    }

    public void setStatus(MessagesStatus status) {
        this.status = status;
    }


    public void setImage(Image image) {
        this.image = image;
    }

    public Image getImage() {
        return image;
    }

    public SpeedMessage getAction() {
        return action;
    }

    public void setAction(SpeedMessage action) {
        this.action = action;
    }



    @Override
    public boolean equals(Object o) {
        Message message = (Message) o;
        return id.equals(message.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static class Image {
        private String url;

        public Image(String url) {
            this.url = url;
        }

        public String getUrl() {
            return this.url;
        }

        public void setUrl(@NotNull String filePath) {
            this.url = filePath;
        }
    }

    public static class Voice {

        private  String url;
        private final int duration;
        private String status = "";


        public Voice(String url, int duration) {
            this.url = url;
            this.duration = duration;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url)
        {
            this.url = url;
        }

        public int getDuration() {
            return duration;
        }

        public String getStatus() {
            return status;
        }


        public void setStatus(String status) {
            this.status = status;
        }

    }


    public static class Video {

        private final int duration;
        private String url;
        private String status = "";


        public Video(String url, int duration, Integer width, Integer height) {
            this.url = url;
            this.duration = duration;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getDuration() {
            return duration;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

    }

}
