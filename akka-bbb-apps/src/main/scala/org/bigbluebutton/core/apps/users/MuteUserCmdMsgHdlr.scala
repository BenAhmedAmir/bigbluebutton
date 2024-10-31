package org.bigbluebutton.core.apps.users

import org.bigbluebutton.common2.msgs.MuteUserCmdMsg
import org.bigbluebutton.core.apps.{PermissionCheck, RightsManagementTrait}
import org.bigbluebutton.core.apps.voice.VoiceApp
import org.bigbluebutton.core.models.{Roles, Users2x, VoiceUsers}
import org.bigbluebutton.core.running.{LiveMeeting, OutMsgRouter}
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
            log.info("Moderator mute/unmute request. meetingId=" + meetingId +
              " moderatorId=" + msg.header.userId +
              " userId=" + u.intId +
              " mute=" + msg.body.mute)
            VoiceApp.muteUserInVoiceConf(
              liveMeeting,
              outGW,
              u.intId,
              msg.body.mute
            )
            // Update mutedBy to moderator's ID when muting, clear it when unmuting
            val newMutedBy = if (msg.body.mute) Some(msg.header.userId) else None
            VoiceUsers.userMuted(liveMeeting.voiceUsers, u.voiceUserId, msg.body.mute, newMutedBy.getOrElse(""))
            log.info("Updated mute status by moderator. mutedBy=" + newMutedBy)
          }
        } else if (msg.body.userId == msg.header.userId) {
          // Handle self mute/unmute for regular users
          log.info("Self mute/unmute request. mute=" + msg.body.mute +
            " mutedBy=" + u.mutedBy +
            " userId=" + msg.header.userId +
            " userMuted=" + u.muted)

          // Check if user was muted by a moderator
          val mutedByModerator = u.mutedBy match {
            case Some(muterId) =>
              Users2x.findWithIntId(liveMeeting.users2x, muterId).exists(_.role == Roles.MODERATOR_ROLE)
            case None => false
          }

          // Allow muting self anytime, but only allow unmuting if not muted by a moderator
          if (msg.body.mute || (!msg.body.mute && !mutedByModerator)) {
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
            log.info("Viewer cannot unmute themselves because they were muted by a moderator.")
          }
        }
      }
      }
    }
  }
