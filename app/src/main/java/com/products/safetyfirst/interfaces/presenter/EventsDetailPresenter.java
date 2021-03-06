package com.products.safetyfirst.interfaces.presenter;


import com.products.safetyfirst.models.Event_model;

/**
 * Created by vikas on 12/10/17.
 */

public interface EventsDetailPresenter {

    void requestEvent();

    void getEvent(Event_model event);

    void onDestroy();

    void setBookMark(String mEventKey);

    void setAction(int action);

    void onActionError(String message);

    void  onActionSuccess();
}
