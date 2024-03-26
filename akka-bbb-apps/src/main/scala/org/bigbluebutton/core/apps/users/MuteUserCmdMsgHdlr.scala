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
            log.inf(mutedByModeratorSet)
            // Update the mutedByModeratorSet if the moderator mutes the user
            if (requester.role == Roles.MODERATOR_ROLE) {
              if (msg.body.mute) {
                mutedByModeratorSet += u.intId
              } else {
                mutedByModeratorSet -= u.intId
              }
            }
          }
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
2024-03-26T11:14:46.670Z INFO  o.b.core.apps.users.UsersApp - you are muted by moderator
  2024-03-26T11:14:46.670Z INFO  o.b.core.apps.users.UsersApp - Received mute user request. meetingId=43b39f7e543e3fd7744c85d90d03ab616d00de61-1711451368884 userId=w_cf9vtonulhgs
  2024-03-26T11:14:46.671Z INFO  o.b.core.apps.users.UsersApp - Send mute user request. meetingId=43b39f7e543e3fd7744c85d90d03ab616d00de61-1711451368884 userId=w_cf9vtonulhgs user=VoiceUserState(w_cf9vtonulhgs,6,none,ag+ben,w_cf9vtonulhgs_1-bbbID-ag+ben,#5e35b1,false,true,false,freeswitch,1711451685190,false,0,false,eab2a55b-b147-469b-b951-5989bc016562)
  2024-03-26T11:14:46.671Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"MuteUserInVoiceConfSysMsg","routing":{"sender":"bbb-apps-akka"},"timestamp":1711451686670},"core":{"header":{"name":"MuteUserInVoiceConfSysMsg","meetingId":"43b39f7e543e3fd7744c85d90d03ab616d00de61-1711451368884"},"body":{"voiceConf":"35739","voiceUserId":"6","mute":true}}}
  2024-03-26T11:14:46.678Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"UserMutedVoiceEvtMsg","routing":{"msgType":"BROADCAST_TO_MEETING","meetingId":"43b39f7e543e3fd7744c85d90d03ab616d00de61-1711451368884","userId":"w_cf9vtonulhgs"},"timestamp":1711451686678},"core":{"header":{"name":"UserMutedVoiceEvtMsg","meetingId":"43b39f7e543e3fd7744c85d90d03ab616d00de61-1711451368884","userId":"w_cf9vtonulhgs"},"body":{"voiceConf":"35739","intId":"w_cf9vtonulhgs","voiceUserId":"w_cf9vtonulhgs","muted":true}}}
  2024-03-26T11:14:48.729Z INFO  o.b.e.redis.LearningDashboardActor - Learning Dashboard data sent for meeting 43b39f7e543e3fd7744c85d90d03ab616d00de61-1711451368884
  2024-03-26T11:14:50.698Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"ToggleListenOnlyModeSysMsg","routing":{"sender":"bbb-apps-akka"},"timestamp":1711451690698},"core":{"header":{"name":"ToggleListenOnlyModeSysMsg","meetingId":"43b39f7e543e3fd7744c85d90d03ab616d00de61-1711451368884"},"body":{"voiceConf":"35739","userId":"w_cf9vtonulhgs","enabled":true}}}