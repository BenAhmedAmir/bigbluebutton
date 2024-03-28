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
    // Log the user object
    log.info("User found: " + u)

    // Your existing code continues...
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
        outGW.send(event)
        var mutedByModerator = u.mutedByModerator

        // Update the mutedByModerator flag if the moderator mutes the user
        if (requester.role == Roles.MODERATOR_ROLE && msg.body.mute) {
          log.info("################################")
          log.info("################################")
          log.info("################################")
          log.info(u.mutedByModerator.toString)
          mutedByModerator = true
          log.info(u.mutedByModerator.toString)
          log.info("################################")
          log.info("################################")
          log.info("################################")
        }
      }
    }

    // Prevent self-unmuting if the user was muted by a moderator
    if (u.mutedByModerator && msg.body.userId == msg.header.userId && !msg.body.mute) {
      // Muted by moderator, and trying to unmute oneself. Do not allow.
      log.info("################################")
      log.info("################################")
      log.info("################################")
      log.info("you can not open microphone")
      log.info("################################")
      log.info("################################")
    }
  }

}
