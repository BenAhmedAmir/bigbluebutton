def handleMuteUserCmdMsg(msg: MuteUserCmdMsg) {
  val unmuteDisabled = !liveMeeting.props.usersProp.allowModsToUnmuteUsers && msg.body.mute == false
  if (msg.body.userId != msg.header.userId && (unmuteDisabled || permissionFailed(
    PermissionCheck.MOD_LEVEL,
    PermissionCheck.VIEWER_LEVEL, liveMeeting.users2x, msg.header.userId
  ))) {
    val meetingId = liveMeeting.props.meetingProp.intId
    val muteUnmuteStr: String = if (msg.body.mute) "mute" else "unmute"
    val reason = "No permission to " + muteUnmuteStr + " user."
    PermissionCheck.ejectUserForFailedPermission(meetingId, msg.header.userId, reason, outGW, liveMeeting)
  } else {
    val meetingId = liveMeeting.props.meetingProp.intId
    val voiceConf = liveMeeting.props.voiceProp.voiceConf

    log.info("Received mute user request. meetingId=" + meetingId + " userId="
      + msg.body.userId)

    val permissions = MeetingStatus2x.getPermissions(liveMeeting.status)
    for {
      requester <- Users2x.findWithIntId(
        liveMeeting.users2x,
        msg.header.userId
      )
      u <- VoiceUsers.findWithIntId(
        liveMeeting.voiceUsers,
        msg.body.userId
      )
    } yield {

      if (requester.role != Roles.MODERATOR_ROLE
        && u.muted && u.mutedBy == Roles.MODERATOR_ROLE
        && msg.body.userId == msg.header.userId) {
        // unmuting self while not moderator and was muted by a moderator. Do not allow.
      } else {
        if (u.muted != msg.body.mute) {
          log.info("Send mute user request. meetingId=" + meetingId + " userId=" + u.intId + " user=" + u)
          val event = MsgBuilder.buildMuteUserInVoiceConfSysMsg(
            meetingId,
            voiceConf,
            u.voiceUserId,
            msg.body.mute
          )
          outGW.send(event)
          VoiceUsers.userMuted(liveMeeting.voiceUsers, u.voiceUserId, msg.body.mute, msg.header.userId) // Set the mutedBy field here
        }
      }
    }
  }
}
