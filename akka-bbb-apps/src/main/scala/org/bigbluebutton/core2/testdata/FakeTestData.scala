package org.bigbluebutton.core2.testdata

import org.bigbluebutton.core.models._
import org.bigbluebutton.core.running.LiveMeeting

/**
 * Create fake test data so we can populate meeting.
 */
trait FakeTestData {

  def createFakeUsers(liveMeeting: LiveMeeting): Unit = {
    val mod1 = createUserVoiceAndCam(liveMeeting, Roles.MODERATOR_ROLE, false, false, CallingWith.WEBRTC, muted = false,
      talking = true, listenOnly = false)
    Users2x.add(liveMeeting.users2x, mod1)

    val mod2 = createUserVoiceAndCam(liveMeeting, Roles.MODERATOR_ROLE, guest = false, authed = true, CallingWith.WEBRTC, muted = false,
      talking = false, listenOnly = false)
    Users2x.add(liveMeeting.users2x, mod2)

    val guest1 = createUserVoiceAndCam(liveMeeting, Roles.VIEWER_ROLE, guest = true, authed = true, CallingWith.WEBRTC, muted = false,
      talking = false, listenOnly = false)
    Users2x.add(liveMeeting.users2x, guest1)
    val guestWait1 = GuestWaiting(guest1.intId, guest1.name, guest1.role, guest1.guest, "", "#ff6242", guest1.authed, System.currentTimeMillis())
    GuestsWaiting.add(liveMeeting.guestsWaiting, guestWait1)

    val guest2 = createUserVoiceAndCam(liveMeeting, Roles.VIEWER_ROLE, guest = true, authed = true, CallingWith.FLASH, muted = false,
      talking = false, listenOnly = false)
    Users2x.add(liveMeeting.users2x, guest2)
    val guestWait2 = GuestWaiting(guest2.intId, guest2.name, guest2.role, guest2.guest, "", "#ff6242", guest2.authed, System.currentTimeMillis())
    GuestsWaiting.add(liveMeeting.guestsWaiting, guestWait2)

    val vu1 = FakeUserGenerator.createFakeVoiceOnlyUser(CallingWith.PHONE, muted = false, talking = false, listenOnly = false)
    VoiceUsers.add(liveMeeting.voiceUsers, vu1)

    val vu2 = FakeUserGenerator.createFakeVoiceOnlyUser(CallingWith.PHONE, muted = false, talking = false, listenOnly = false)
    VoiceUsers.add(liveMeeting.voiceUsers, vu2)
    val vu3 = FakeUserGenerator.createFakeVoiceOnlyUser(CallingWith.PHONE, muted = false, talking = false, listenOnly = false)
    VoiceUsers.add(liveMeeting.voiceUsers, vu3)
    val vu4 = FakeUserGenerator.createFakeVoiceOnlyUser(CallingWith.PHONE, muted = false, talking = false, listenOnly = false)
    VoiceUsers.add(liveMeeting.voiceUsers, vu4)
    val vu5 = FakeUserGenerator.createFakeVoiceOnlyUser(CallingWith.PHONE, muted = false, talking = false, listenOnly = false)
    VoiceUsers.add(liveMeeting.voiceUsers, vu5)

    for (i <- 1 to 50) {
      val guser = createUserVoiceAndCam(liveMeeting, Roles.MODERATOR_ROLE, guest = false, authed = true, CallingWith.WEBRTC, muted = false,
        talking = false, listenOnly = false)
      Users2x.add(liveMeeting.users2x, guser)
    }
  }

  def createUserVoiceAndCam(liveMeeting: LiveMeeting, role: String, guest: Boolean, authed: Boolean, callingWith: String,
                            muted: Boolean, talking: Boolean, listenOnly: Boolean): UserState = {

    val ruser1 = FakeUserGenerator.createFakeRegisteredUser(liveMeeting.registeredUsers, Roles.MODERATOR_ROLE, true, false)

    val vuser1 = FakeUserGenerator.createFakeVoiceUser(ruser1, "webrtc", muted = false, talking = true, listenOnly = false)
    VoiceUsers.add(liveMeeting.voiceUsers, vuser1)

    val rusers = Users2x.findAll(liveMeeting.users2x)
    val others = rusers.filterNot(u => u.intId == ruser1.id)
    val subscribers = others.map { o => o.intId }
    val wstream1 = FakeUserGenerator.createFakeWebcamStreamFor(ruser1.id, subscribers.toSet)
    Webcams.addWebcamStream(liveMeeting.webcams, wstream1)

    createFakeUser(liveMeeting, ruser1)
  }

  def createFakeUser(liveMeeting: LiveMeeting, regUser: RegisteredUser): UserState = {
    UserState(intId = regUser.id, extId = regUser.externId, name = regUser.name, role = regUser.role, pin = false,
      mobile = false, guest = regUser.guest, authed = regUser.authed, guestStatus = regUser.guestStatus,
      emoji = "none", reactionEmoji = "none", raiseHand = false, away = false, locked = false, presenter = false,
      avatar = regUser.avatarURL, color = "#ff6242", clientType = "unknown",
      pickExempted = false, userLeftFlag = UserLeftFlag(false, 0))
  }

}
2024-03-27T10:32:07.083Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{
  "name":"UserMutedVoiceEvtMsg","routing":{
  "msgType":"BROADCAST_TO_MEETING","meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","userId":"w_bczylgrpz5eb"}
  ,"timestamp":1711535527083
}
  ,"core":{
    "header":{
    "name":"UserMutedVoiceEvtMsg","meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","userId":"w_bczylgrpz5eb"
  },
    "body":{
    "voiceConf":"03138","intId":"w_bczylgrpz5eb","voiceUserId":"w_bczylgrpz5eb","muted":true,"mutedByModerator":false}
  }}
  2024-03-27T10:32:07.754Z INFO  o.b.e.redis.LearningDashboardActor - Learning Dashboard data sent for meeting 3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240
  2024-03-27T10:32:11.094Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{
    "name":"ToggleListenOnlyModeSysMsg","routing":{
    "sender":"bbb-apps-akka"
  },"timestamp":1711535531094}
    ,"core":{"header":{"name":"ToggleListenOnlyModeSysMsg","meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240"},"body":{"voiceConf":"03138","userId":"w_bczylgrpz5eb","enabled":true}}}
  2024-03-27T10:32:11.562Z INFO  o.b.core.apps.users.UsersApp - you are muted by moderator
  2024-03-27T10:32:11.562Z INFO  o.b.core.apps.users.UsersApp - Received mute user request. meetingId=3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240 userId=w_bczylgrpz5eb
  2024-03-27T10:32:11.562Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"MuteUserCmdMsg","routing":{"meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","userId":"w_bczylgrpz5eb"},"timestamp":1711535531560},"core":{"header":{"name":"MuteUserCmdMsg","meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","userId":"w_bczylgrpz5eb"},"body":{"userId":"w_bczylgrpz5eb","mutedBy":"w_bczylgrpz5eb","mute":true}}}
  2024-03-27T10:32:15.394Z INFO  o.b.core.apps.users.UsersApp - you are muted by moderator
  2024-03-27T10:32:15.394Z INFO  o.b.core.apps.users.UsersApp - Received mute user request. meetingId=3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240 userId=w_bczylgrpz5eb
  2024-03-27T10:32:15.394Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"MuteUserCmdMsg","routing":{"meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","userId":"w_bczylgrpz5eb"},"timestamp":1711535535392},"core":{"header":{"name":"MuteUserCmdMsg","meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","userId":"w_bczylgrpz5eb"},"body":{"userId":"w_bczylgrpz5eb","mutedBy":"w_bczylgrpz5eb","mute":true}}}
  2024-03-27T10:32:20.612Z INFO  o.b.core.apps.users.UsersApp - you are muted by moderator
  2024-03-27T10:32:20.612Z INFO  o.b.core.apps.users.UsersApp - Received mute user request. meetingId=3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240 userId=w_bczylgrpz5eb
  2024-03-27T10:32:20.612Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"MuteUserCmdMsg","routing":{"meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","userId":"w_bczylgrpz5eb"},"timestamp":1711535540611},"core":{"header":{"name":"MuteUserCmdMsg","meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","userId":"w_bczylgrpz5eb"},"body":{"userId":"w_bczylgrpz5eb","mutedBy":"w_bczylgrpz5eb","mute":true}}}
  2024-03-27T10:33:39.555Z INFO  o.b.core.running.MeetingActor - Received user left meeting. user w_bczylgrpz5eb meetingId=3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240
  2024-03-27T10:33:39.555Z INFO  o.b.core.running.MeetingActor - Setting user left flag. user w_bczylgrpz5eb meetingId=3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240
  2024-03-27T10:33:39.601Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"UserLeftVoiceConfEvtMsg","routing":{"voiceConf":"03138"},"timestamp":1711535619598},"core":{"header":{"name":"UserLeftVoiceConfEvtMsg","voiceConf":"03138"},"body":{"voiceConf":"03138","voiceUserId":"27"}}}
  2024-03-27T10:33:39.601Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"UserLeftVoiceConfToClientEvtMsg","routing":{"msgType":"BROADCAST_TO_MEETING","meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","userId":"w_bczylgrpz5eb"},"timestamp":1711535619600},"core":{"header":{"name":"UserLeftVoiceConfToClientEvtMsg","meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","userId":"w_bczylgrpz5eb"},"body":{"voiceConf":"03138","intId":"w_bczylgrpz5eb","voiceUserId":"w_bczylgrpz5eb"}}}
  2024-03-27T10:33:39.618Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"VoiceConfCallStateEvtMsg","routing":{"voiceConf":"03138"},"timestamp":1711535619617},"core":{"header":{"name":"VoiceConfCallStateEvtMsg","voiceConf":"03138"},"body":{"voiceConf":"03138","callSession":"1562cecc-1bab-492a-b428-5e4a6a77dfc2","clientSession":"1","userId":"w_bczylgrpz5eb","callerName":"Ahmed","callState":"CALL_ENDED","origCallerIdName":"w_bczylgrpz5eb_1-bbbID-Ahmed","origCalledDest":"03138"}}}
  2024-03-27T10:33:39.618Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"VoiceCallStateEvtMsg","routing":{"msgType":"BROADCAST_TO_MEETING","meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","userId":"03138"},"timestamp":1711535619618},"core":{"header":{"name":"VoiceCallStateEvtMsg","meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","userId":"03138"},"body":{"meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","voiceConf":"03138","clientSession":"1","userId":"w_bczylgrpz5eb","callerName":"Ahmed","callState":"CALL_ENDED"}}}
  2024-03-27T10:33:47.754Z INFO  o.b.e.redis.LearningDashboardActor - Learning Dashboard data sent for meeting 3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240
  2024-03-27T10:33:52.027Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"LogoutAndEndMeetingCmdMsg","routing":{"meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","userId":"w_rdqvck0cbrzd"},"timestamp":1711535632025},"core":{"header":{"name":"LogoutAndEndMeetingCmdMsg","meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","userId":"w_rdqvck0cbrzd"},"body":{"userId":"w_rdqvck0cbrzd"}}}
  2024-03-27T10:33:52.028Z INFO  o.b.core.apps.users.UsersApp - Meeting 3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240 ended by user [w_rdqvck0cbrzd, amirbenahmed} when logging out.
  2024-03-27T10:33:52.028Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"MeetingEndingEvtMsg","routing":{"msgType":"BROADCAST_TO_MEETING","meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","userId":"w_rdqvck0cbrzd"},"timestamp":1711535632026},"core":{"header":{"name":"MeetingEndingEvtMsg","meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","userId":"w_rdqvck0cbrzd"},"body":{"meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","reason":"ENDED_AFTER_USER_LOGGED_OUT"}}}
  2024-03-27T10:33:52.030Z INFO  o.b.e.redis.LearningDashboardActor - Learning Dashboard data sent for meeting 3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240
  2024-03-27T10:33:52.030Z INFO  o.b.e.redis.LearningDashboardActor -  removed for meeting 3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240.
  2024-03-27T10:33:52.038Z WARN  o.b.core.BigBlueButtonActor - Cannot handle DestroyMeetingSysCmdMsg
  2024-03-27T10:33:52.058Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"UserLeftVoiceConfEvtMsg","routing":{"voiceConf":"03138"},"timestamp":1711535632056},"core":{"header":{"name":"UserLeftVoiceConfEvtMsg","voiceConf":"03138"},"body":{"voiceConf":"03138","voiceUserId":"26"}}}
  2024-03-27T10:33:52.058Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"VoiceConfCallStateEvtMsg","routing":{"voiceConf":"03138"},"timestamp":1711535632057},"core":{"header":{"name":"VoiceConfCallStateEvtMsg","voiceConf":"03138"},"body":{"voiceConf":"03138","callSession":"1562cecc-1bab-492a-b428-5e4a6a77dfc2","clientSession":"3","userId":"w_rdqvck0cbrzd","callerName":"amirbenahmed","callState":"CALL_ENDED","origCallerIdName":"w_rdqvck0cbrzd_3-bbbID-amirbenahmed","origCalledDest":"03138"}}}
  2024-03-27T10:33:54.544Z INFO  o.b.core.BigBlueButtonActor - Destroyed meetingId=3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240
  2024-03-27T10:33:54.544Z INFO  o.b.core2.AnalyticsActor - -- analytics -- {"envelope":{"name":"DisconnectAllClientsSysMsg","routing":{"msgType":"SYSTEM","meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","userId":"not-used"},"timestamp":1711535634543},"core":{"header":{"name":"DisconnectAllClientsSysMsg","meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240"},"body":{"meetingId":"3602882fdb989ce1b22d6cfeb3562cb5832b7a9f-1711535433240","reason":"meeting-destroyed"}}}