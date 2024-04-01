package org.bigbluebutton.core.apps.users

import org.bigbluebutton.common2.msgs.MuteUserCmdMsg
import org.bigbluebutton.core.apps.{ PermissionCheck, RightsManagementTrait }
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

      log.info("Received mute user request. meetingId=" + meetingId + " userId=" + msg.body.userId)

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
        log.info("u.mutedBy ::::::::  " + u.mutedBy)
        log.info("msg.body.userId :::::::::: "+ msg.body.userId)
        log.info("u.muted :::::::: " +u.muted)
        log.info("requester.role ====== " +requester.role)
        log.info("msg.header.userId ====== " +msg.header.userId)
        if (requester.role != Roles.MODERATOR_ROLE
          && u.muted && u.mutedBy == Roles.MODERATOR_ROLE
          && msg.body.userId == msg.header.userId) {
          // unmuting self while not moderator and was muted by a moderator. Do not allow.
          log.info("######################################################")
          log.info("######################################################")
          log.info("######################################################")
          log.info("######################################################")
          log.info("MUTED BY MODERATOR")
          log.info("######################################################")
          log.info("######################################################")
          log.info("######################################################")
          log.info("######################################################")
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
            log.info("logloglogloglogloglogloglog"+event)
            VoiceUsers.userMuted(liveMeeting.voiceUsers, u.voiceUserId, msg.body.mute, msg.header.userId)
            log.info("######################################################")
            log.info("######################################################")
            log.info("######################################################")
            log.info("######################################################")
            log.info("######################################################")
            log.info("######################################################")
            log.info("######################################################")
            log.info("######################################################")
          }
        }
      }
    }
  }

}
