package org.bigbluebutton.core.apps.users

import org.bigbluebutton.common2.msgs.MuteUserCmdMsg
import org.bigbluebutton.core.apps.{PermissionCheck, RightsManagementTrait}
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
      log.info("you are muted by moderator")
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

        // Define a set to store user IDs muted by the moderator
        var mutedByModeratorSet: Set[String] = Set()

        if (requester.role != Roles.MODERATOR_ROLE && permissions.disableMic && requester.locked && u.muted && msg.body.userId == msg.header.userId) {
          // Non-moderator user trying to unmute another user of lower role while microphone is disabled. Do not allow.
        } else {
          if (u.muted != msg.body.mute) {
            log.info("Send mute user request. meetingId=" + meetingId + " userId=" + u.intId + " user=" + u)
            val event = MsgBuilder.buildMuteUserInVoiceConfSysMsg(
              meetingId,
              voiceConf,
              u.voiceUserId,
              msg.body.mute
            )
            // Update the mutedByModeratorSet if the moderator mutes the user
            if (requester.role == Roles.MODERATOR_ROLE) {
              if (msg.body.mute) {
                mutedByModeratorSet += u.intId
              } else {
                mutedByModeratorSet -= u.intId
              }
              log.info("mutedByModeratorSet: " + mutedByModeratorSet.mkString(", "))

            }
            outGW.send(event)

          }
        }

        // Prevent self-unmuting if the user was muted by a moderator
        if (mutedByModeratorSet.contains(msg.body.userId) && msg.body.userId == msg.header.userId && !msg.body.mute) {
          log.info("you can not open your microphone because you are muted by moderator")
        }


      }
    }

  }
}
