if(token==null){
    if(!unprivilegedAppCanCreateTokenWith(parentWindow,callingUid,type,
        rootType,attrs.token,attrs.packageName)){
           return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
    }
final IBinder binder=attrs.token!=null?attrs.token:client.asBinder();
    token=new WindowToken(this,binder,type,false,displayContent,
            session.mCanAddInternalSystemWindow,isRoundedCornerOverlay);
}else if(rootType>=FIRST_APPLICATION_WINDOW && rootType<=LAST_APPLICATION_WINDOW){
        activity=token.asActivityRecord();
        if(activity==null){
            ProtoLog.w(WM_ERROR,"Attempted to add window with non-application token "
                +".%s Aborting.",token);
                    return WindowManagerGlobal.ADD_NOT_APP_TOKEN;
        }else if(activity.getParent()==null){
            ProtoLog.w(WM_ERROR,"Attempted to add window with exiting application token "
                +".%s Aborting.",token);
                    return WindowManagerGlobal.ADD_APP_EXITING;
        }else if(type==TYPE_APPLICATION_STARTING&&activity.startingWindow!=null){
            ProtoLog.w(WM_ERROR, "Attempted to add starting window to token with already existing"
                +" starting window");
                    return WindowManagerGlobal.ADD_DUPLICATE_ADD;
        } else if(rootType==TYPE_INPUT_METHOD){
            if(token.windowType!=TYPE_INPUT_METHOD){
                ProtoLog.w(WM_ERROR,"Attempted to add input method window with bad token "
                    +"%s.  Aborting.",attrs.token);
                        return WindowManagerGlobal.ADD_BAD_APP_TOKEN;
            }
        }