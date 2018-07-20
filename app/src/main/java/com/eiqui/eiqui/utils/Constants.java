package com.eiqui.eiqui.utils;

/**
 * Created by uchar on 11/09/16.
 */
public class Constants {

    static public final Integer ID_STAGE_REALIZADO = 7;
    static public final Integer ID_STAGE_CANCELADO = 8;

    static public final String OD_STAGE_OPEN = "['stage_id', 'not in', ["+ ID_STAGE_REALIZADO +","+ ID_STAGE_CANCELADO +"]]";
    static public final String OD_KANBAN_NOT_BLOCKED = "['kanban_state', '!=', 'blocked']";
    static public final String OD_USER_ID = "['user_id', '=', %d]";
    static public final String OD_USER_ID_IN_MEMBER = "['members', 'in', [%d]]";

    public static final String ACTION_INIT_SERVICE = "eiqui.android.action.INIT_SERVICE";

    public static final String SHARED_PREFS_USER_INFO = "UserInfo";

}
