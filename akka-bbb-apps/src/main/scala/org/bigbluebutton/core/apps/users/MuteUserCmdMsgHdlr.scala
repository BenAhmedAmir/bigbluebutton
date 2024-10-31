package org.bigbluebutton.core.apps.users

import org.bigbluebutton.common2.msgs.MuteUserCmdMsg
import org.bigbluebutton.core.apps.{ PermissionCheck, RightsManagementTrait }
import org.bigbluebutton.core.apps.voice.VoiceApp
import org.bigbluebutton.core.models.{ Roles, Users2x, VoiceUsers }
import org.bigbluebutton.core.running.{ LiveMeeting, OutMsgRouter }
import org.bigbluebutton.core2.MeetingStatus2x
import org.bigbluebutton.core2.message.senders.MsgBuilder

trait MuteUserCmdMsgHdlr extends RightsManagementTrait {
  this: UsersApp =>

  val liveMeeting: LiveMeeting
  val outGW: OutMsgRouter

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
          && permissions.disableMic
          && requester.locked
          && u.muted &&
          msg.body.userId == msg.header.userId) {
          // unmuting self while not moderator and mic disabled. Do not allow.
        } else if (requester.role == Roles.MODERATOR_ROLE) {
          // Allow moderators to mute/unmute anyone
          if (u.muted != msg.body.mute) {
            log.info("Send mute/unmute user request. meetingId=" + meetingId + " userId=" + u.intId + " user=" + u)
            VoiceApp.muteUserInVoiceConf(
              liveMeeting,
              outGW,
              u.intId,
              msg.body.mute
            )
            VoiceUsers.userMuted(liveMeeting.voiceUsers, u.voiceUserId, msg.body.mute, msg.header.userId)
          }
        } else if (msg.body.userId == msg.header.userId) {
          // Handle self mute/unmute for regular users
          log.info("Self mute/unmute request. mute=" + msg.body.mute + 
                   " mutedBy=" + u.mutedBy + 
                   " userId=" + msg.header.userId + 
                   " userMuted=" + u.muted)
          
          if (msg.body.mute || (!msg.body.mute && u.mutedBy == msg.header.userId)) {
            // Allow self-muting and self-unmuting if user muted themselves
            log.info("Executing mute/unmute self request. meetingId=" + meetingId + 
                    " userId=" + u.intId + 
                    " user=" + u)
            VoiceApp.muteUserInVoiceConf(
              liveMeeting,
              outGW,
              u.intId,
              msg.body.mute
            )
            VoiceUsers.userMuted(liveMeeting.voiceUsers, u.voiceUserId, msg.body.mute, msg.header.userId)
          } else {
            log.info("Mute/unmute condition not met. User cannot unmute themselves.")
          }
        }
      }
    }
  }
}
