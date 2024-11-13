import React, { useEffect, useState } from 'react';
import { withTracker } from 'meteor/react-meteor-data';
import UserListService from '/imports/ui/components/user-list/service';
import UserParticipants from './component';
import { meetingIsBreakout } from '/imports/ui/components/app/service';
import ChatService from '/imports/ui/components/chat/service';
import Auth from '/imports/ui/services/auth';
import useContextUsers from '/imports/ui/components/components-data/users-context/service';
import VideoService from '/imports/ui/components/video-provider/service';
import UserReactionService from '/imports/ui/components/user-reaction/service';
import WhiteboardService from '/imports/ui/components/whiteboard/service';
import Meetings from '/imports/api/meetings';

const UserParticipantsContainer = (props) => {
  const {
    formatUsers,
    setEmojiStatus,
    setUserAway,
    clearAllEmojiStatus,
    clearAllReactions,
    roving,
    requestUserInformation,
    muteAllExceptPresenter,
  } = UserListService;

  const { videoUsers, whiteboardUsers, reactionUsers, isModerator } = props;
  const { users: contextUsers, isReady } = useContextUsers();

  const [searchQuery, setSearchQuery] = useState('');

  const currentUser =
    contextUsers && isReady ? contextUsers[Auth.meetingID][Auth.userID] : null;
  const usersArray =
    contextUsers && isReady
      ? Object.values(contextUsers[Auth.meetingID])
      : null;
  const users =
    contextUsers && isReady
      ? formatUsers(usersArray, videoUsers, whiteboardUsers, reactionUsers)
      : [];

  console.log(users);
  const filteredUsers = users
    ? users?.filter((user) =>
        user.name.toLowerCase().includes(searchQuery.toLowerCase())
      )
    : [];
  const handleDownAllHands = () => {
    clearAllReactions();
    clearAllEmojiStatus();
  };
  const handleMuteAll = () => {
    muteAllExceptPresenter();
  };
  return (
    <>
      <input
        type='text'
        style={{
          padding: '10px',
          borderRadius: '10px',
          margin: '15px',
          border: '1px solid #2ba7df',
          visibility: isModerator ? 'visible' : 'hidden',
        }}
        placeholder='Search by name'
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
      />
      <div style={{ display: 'flex', gap: '10px' }}>
        {' '}
        <button
          onClick={handleDownAllHands}
          style={{
            padding: '8px 12px',
            borderRadius: '8px',
            margin: '10px',
            backgroundColor: '#2ba7df',
            color: '#fff',
            border: 'none',
            cursor: 'pointer',
            visibility: isModerator ? 'visible' : 'hidden',
          }}
        >
          Down All Hands
        </button>
        <button
          onClick={handleMuteAll}
          style={{
            padding: '8px 12px',
            borderRadius: '8px',
            margin: '10px',
            backgroundColor: '#2ba7df',
            color: '#fff',
            border: 'none',
            cursor: 'pointer',
            visibility: isModerator ? 'visible' : 'hidden',
          }}
        >
          Mute All
        </button>
      </div>

      <UserParticipants
        {...{
          currentUser,
          users: filteredUsers,
          setEmojiStatus,
          setUserAway,
          clearAllEmojiStatus,
          clearAllReactions,
          roving,
          requestUserInformation,
          isReady,
          ...props,
        }}
      />
    </>
  );
};

export default withTracker(() => {
  ChatService.removePackagedClassAttribute(
    ['ReactVirtualized__Grid', 'ReactVirtualized__Grid__innerScrollContainer'],
    'role'
  );

  const whiteboardId = WhiteboardService.getCurrentWhiteboardId();
  const whiteboardUsers = whiteboardId
    ? WhiteboardService.getMultiUser(whiteboardId)
    : null;
  const currentMeeting = Meetings.findOne(
    { meetingId: Auth.meetingID },
    { fields: { lockSettingsProps: 1 } }
  );

  const isMeetingMuteOnStart = () => {
    const { voiceProp } = Meetings.findOne(
      { meetingId: Auth.meetingID },
      { fields: { 'voiceProp.muteOnStart': 1 } }
    );
    const { muteOnStart } = voiceProp;
    return muteOnStart;
  };

  return {
    isMeetingMuteOnStart: isMeetingMuteOnStart(),
    meetingIsBreakout: meetingIsBreakout(),
    videoUsers: VideoService.getUsersIdFromVideoStreams(),
    whiteboardUsers,
    reactionUsers: UserReactionService.getUsersIdFromUserReaction(),
    isThisMeetingLocked: UserListService.isMeetingLocked(Auth.meetingID),
    lockSettingsProps: currentMeeting && currentMeeting.lockSettingsProps,
  };
})(UserParticipantsContainer);
