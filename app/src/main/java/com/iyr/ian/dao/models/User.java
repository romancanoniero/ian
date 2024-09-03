package com.iyr.ian.dao.models;


import com.stfalcon.chatkit.commons.models.IUser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;


public  class User extends UserMinimum  {

    public User(@NotNull String type) {
        this.user_type = type;
    }

    public User() {

    }


    @Nullable
    public String registrationType;



    @NotNull
    public String pulse_status = "";
    public int sos_invocation_count = 0;
    public String sos_invocation_method;
    @NotNull
    public String email_address = "";
    @Nullable
    public String telephone_number;
    @Nullable
    public Object allow_speed_dial;
    @NotNull
    public String security_code = "";
    @NotNull
    public String subscription_type_key = "";
    public String status = "";


    @NotNull
    public String getSubscription_type_key() {
        return subscription_type_key;
    }

    public void setSubscription_type_key(@NotNull String subscription_type_key) {
        this.subscription_type_key = subscription_type_key;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getNotification_token() {
        return notification_token;
    }

    public void setNotification_token(String notification_token) {
        this.notification_token = notification_token;
    }

    public Long getBirth_date() {
        return birth_date;
    }

    public void setBirth_date(Long birth_date) {
        this.birth_date = birth_date;
    }

    @NotNull
    public String getPulse_status() {
        return pulse_status;
    }

    public void setPulse_status(@NotNull String pulse_status) {
        this.pulse_status = pulse_status;
    }

    public Boolean getIs_monitoring() {
        return is_monitoring;
    }

    public void setIs_monitoring(Boolean is_monitoring) {
        this.is_monitoring = is_monitoring;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public int getSos_invocation_count() {
        return sos_invocation_count;
    }

    public void setSos_invocation_count(int sos_invocation_count) {
        this.sos_invocation_count = sos_invocation_count;
    }

    public String getSos_invocation_method() {
        return sos_invocation_method;
    }

    public void setSos_invocation_method(String sos_invocation_method) {
        this.sos_invocation_method = sos_invocation_method;
    }

    public String getUser_short_link() {
        return user_short_link;
    }

    public void setUser_short_link(String user_short_link) {
        this.user_short_link = user_short_link;
    }

    @NotNull
    public String getEmail_address() {
        return email_address;
    }

    public void setEmail_address(@NotNull String email_address) {
        this.email_address = email_address;
    }

    @Nullable
    public String getTelephone_number() {
        return telephone_number;
    }

    public void setTelephone_number(@Nullable String telephone_number) {
        this.telephone_number = telephone_number;
    }

    @Nullable
    public Object getAllow_speed_dial() {
        return allow_speed_dial;
    }

    public String first_name;
    public String last_name;
    public String notification_token;
    public Long birth_date;

    public void setAllow_speed_dial(@Nullable Object allow_speed_dial) {
        this.allow_speed_dial = allow_speed_dial;
    }
    public Boolean is_monitoring = false;
    public String user_type;

    @NotNull
    public String getSubscriptionTypeKey() {
        return subscription_type_key;
    }

    public void setSubscriptionTypeKey(@NotNull String subscriptionTypeKey) {
        this.subscription_type_key = subscriptionTypeKey;
    }
    public String user_short_link;

    @NotNull
    public String getSecurity_code() {
        return security_code;
    }

    public void setSecurity_code(@NotNull String security_code) {
        this.security_code = security_code;
    }

    public ArrayList<EventFollowed> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<EventFollowed> events) {
        this.events = events;
    }

    public String getStatus() {
        return status;
    }

    public ArrayList<EventFollowed> events;

    public void setStatus(String status) {
        this.status = status;
    }


}
