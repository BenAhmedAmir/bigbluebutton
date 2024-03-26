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
    var mutedByModeratorSet: Set[String] = Set()
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

        // Add a flag to track if the user was muted by a moderator
        var mutedByModerator: Boolean = false

        if (requester.role != Roles.MODERATOR_ROLE && u.role != Roles.MODERATOR_ROLE && permissions.disableMic && requester.locked && u.muted && msg.body.userId == msg.header.userId) {
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

            // Update the mutedByModerator flag if the moderator mutes the user
            if (requester.role == Roles.MODERATOR_ROLE && msg.body.mute) {
              mutedByModerator = true
            }
          }
        }

        // Prevent self-unmuting if the user was muted by a moderator
        if (mutedByModerator && msg.body.userId == msg.header.userId && !msg.body.mute) {
          // Muted by moderator, and trying to unmute oneself. Do not allow.
        }


        // Prevent self-unmuting if the user was muted by a moderator
        if (mutedByModeratorSet.contains(msg.body.userId) && msg.body.userId == msg.header.userId && !msg.body.mute) {
          // Muted by moderator, and trying to unmute oneself. Do not allow.
          log.info("you are muted by moderator")

        }
      }
    }

  }
}
2024-03-26T12:33:30.217Z INFO  o.b.core.apps.users.UsersApp - you are muted by moderator
  2024-03-26T12:33:30.217Z INFO  o.b.core.apps.users.UsersApp - Received mute user request. meetingId=adc36abec7fe4f731c46350f582f74b7a783bab7-1711456365784 userId=w_gad89hxqi9jc
  2024-03-26T12:33:30.217Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"MuteUserCmdMsg","routing":{"meetingId":"adc36abec7fe4f731c46350f582f74b7a783bab7-1711456365784","userId":"w_cy9drwfopfyp"},"timestamp":1711456410214},"core":{"header":{"name":"MuteUserCmdMsg","meetingId":"adc36abec7fe4f731c46350f582f74b7a783bab7-1711456365784","userId":"w_cy9drwfopfyp"},"body":{"userId":"w_gad89hxqi9jc","mutedBy":"w_cy9drwfopfyp","mute":true}}}
  2024-03-26T12:33:30.217Z INFO  o.b.core.apps.users.UsersApp - Send mute user request. meetingId=adc36abec7fe4f731c46350f582f74b7a783bab7-1711456365784 userId=w_gad89hxqi9jc user=VoiceUserState(w_gad89hxqi9jc,8,none,guezguez,w_gad89hxqi9jc_1-bbbID-guezguez,#5e35b1,false,true,false,freeswitch,1711456408536,true,1711456408501717,false,0fa4b39f-80f7-49d6-84f6-984c2c8a9e37)
  2024-03-26T12:33:30.217Z INFO  o.b.core.apps.users.UsersApp -
    2024-03-26T12:33:30.218Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"MuteUserInVoiceConfSysMsg","routing":{"sender":"bbb-apps-akka"},"timestamp":1711456410217},"core":{"header":{"name":"MuteUserInVoiceConfSysMsg","meetingId":"adc36abec7fe4f731c46350f582f74b7a783bab7-1711456365784"},"body":{"voiceConf":"33987","voiceUserId":"8","mute":true}}}
  2024-03-26T12:33:30.224Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"UserMutedVoiceEvtMsg","routing":{"msgType":"BROADCAST_TO_MEETING","meetingId":"adc36abec7fe4f731c46350f582f74b7a783bab7-1711456365784","userId":"w_gad89hxqi9jc"},"timestamp":1711456410224},"core":{"header":{"name":"UserMutedVoiceEvtMsg","meetingId":"adc36abec7fe4f731c46350f582f74b7a783bab7-1711456365784","userId":"w_gad89hxqi9jc"},"body":{"voiceConf":"33987","intId":"w_gad89hxqi9jc","voiceUserId":"w_gad89hxqi9jc","muted":true}}}
  2024-03-26T12:33:30.408Z INFO  o.b.e.redis.LearningDashboardActor - Learning Dashboard data sent for meeting adc36abec7fe4f731c46350f582f74b7a783bab7-1711456365784
  2024-03-26T12:33:34.238Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"ToggleListenOnlyModeSysMsg","routing":{"sender":"bbb-apps-akka"},"timestamp":1711456414237},"core":{"header":{"name":"ToggleListenOnlyModeSysMsg","meetingId":"adc36abec7fe4f731c46350f582f74b7a783bab7-1711456365784"},"body":{"voiceConf":"33987","userId":"w_gad89hxqi9jc","enabled":true}}}
  2024-03-26T12:33:37.472Z INFO  o.b.core.apps.users.UsersApp - you are muted by moderator
  2024-03-26T12:33:37.473Z INFO  o.b.core.apps.users.UsersApp - Received mute user request. meetingId=adc36abec7fe4f731c46350f582f74b7a783bab7-1711456365784 userId=w_gad89hxqi9jc
  2024-03-26T12:33:37.473Z INFO  o.b.core.apps.users.UsersApp - Send mute user request. meetingId=adc36abec7fe4f731c46350f582f74b7a783bab7-1711456365784 userId=w_gad89hxqi9jc user=VoiceUserState(w_gad89hxqi9jc,8,none,guezguez,w_gad89hxqi9jc_1-bbbID-guezguez,#5e35b1,true,false,false,freeswitch,1711456412112,true,1711456408501717,false,0fa4b39f-80f7-49d6-84f6-984c2c8a9e37)
  2024-03-26T12:33:37.473Z INFO  o.b.core.apps.users.UsersApp -
    2024-03-26T12:33:37.473Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"MuteUserCmdMsg","routing":{"meetingId":"adc36abec7fe4f731c46350f582f74b7a783bab7-1711456365784","userId":"w_gad89hxqi9jc"},"timestamp":1711456417470},"core":{"header":{"name":"MuteUserCmdMsg","meetingId":"adc36abec7fe4f731c46350f582f74b7a783bab7-1711456365784","userId":"w_gad89hxqi9jc"},"body":{"userId":"w_gad89hxqi9jc","mutedBy":"w_gad89hxqi9jc","mute":false}}}
  2024-03-26T12:33:37.473Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"MuteUserInVoiceConfSysMsg","routing":{"sender":"bbb-apps-akka"},"timestamp":1711456417472},"core":{"header":{"name":"MuteUserInVoiceConfSysMsg","meetingId":"adc36abec7fe4f731c46350f582f74b7a783bab7-1711456365784"},"body":{"voiceConf":"33987","voiceUserId":"8","mute":false}}}
  2024-03-26T12:33:37.480Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"ToggleListenOnlyModeSysMsg","routing":{"sender":"bbb-apps-akka"},"timestamp":1711456417479},"core":{"header":{"name":"ToggleListenOnlyModeSysMsg","meetingId":"adc36abec7fe4f731c46350f582f74b7a783bab7-1711456365784"},"body":{"voiceConf":"33987","userId":"w_gad89hxqi9jc","enabled":false}}}
  2024-03-26T12:33:37.480Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"UserMutedVoiceEvtMsg","routing":{"msgType":"BROADCAST_TO_MEETING","meetingId":"adc36abec7fe4f731c46350f582f74b7a783bab7-1711456365784","userId":"w_gad89hxqi9jc"},"timestamp":1711456417480},"core":{"header":{"name":"UserMutedVoiceEvtMsg","meetingId":"adc36abec7fe4f731c46350f582f74b7a783bab7-1711456365784","userId":"w_gad89hxqi9jc"},"body":{"voiceConf":"33987","intId":"w_gad89hxqi9jc","voiceUserId":"w_gad89hxqi9jc","muted":false}}}
  2024-03-26T12:33:40.408Z INFO  o.b.e.redis.LearningDashboardActor - Learning Dashboard data sent for meeting adc36abec7fe4f731c46350f582f74b7a783bab7-1711456365784
