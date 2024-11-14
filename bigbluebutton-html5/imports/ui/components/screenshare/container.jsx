import React, { useContext, useState } from 'react';
import { withTracker } from 'meteor/react-meteor-data';
import Auth from '/imports/ui/services/auth';
import {
  getSharingContentType,
  getBroadcastContentType,
  isScreenGloballyBroadcasting,
  isCameraAsContentGloballyBroadcasting,
  isScreenBroadcasting,
  isCameraAsContentBroadcasting,
} from './service';
import ScreenshareComponent from './component';
import {
  layoutSelect,
  layoutSelectOutput,
  layoutDispatch,
} from '../layout/context';
import getFromUserSettings from '/imports/ui/services/users-settings';
import { UsersContext } from '/imports/ui/components/components-data/users-context/context';
import AudioService from '/imports/ui/components/audio/service';
import { shouldEnableVolumeControl } from './service';
import MediaService from '/imports/ui/components/media/service';
import NotesService from '/imports/ui/components/notes/service';

import { defineMessages } from 'react-intl';

const screenshareIntlMessages = defineMessages({
  label: {
    id: 'app.screenshare.screenShareLabel',
    description: 'screen share area element label',
  },
  presenterLoadingLabel: { id: 'app.screenshare.presenterLoadingLabel' },
  viewerLoadingLabel: { id: 'app.screenshare.viewerLoadingLabel' },
  presenterSharingLabel: { id: 'app.screenshare.presenterSharingLabel' },
  autoplayBlockedDesc: { id: 'app.media.screenshare.autoplayBlockedDesc' },
  autoplayAllowLabel: { id: 'app.media.screenshare.autoplayAllowLabel' },
  started: {
    id: 'app.media.screenshare.start',
    description: 'toast to show when a screenshare has started',
  },
  ended: {
    id: 'app.media.screenshare.end',
    description: 'toast to show when a screenshare has ended',
  },
  endedDueToDataSaving: {
    id: 'app.media.screenshare.endDueToDataSaving',
    description:
      'toast to show when a screenshare has ended by changing data savings option',
  },
});

const cameraAsContentIntlMessages = defineMessages({
  label: {
    id: 'app.cameraAsContent.cameraAsContentLabel',
    description: 'screen share area element label',
  },
  presenterLoadingLabel: { id: 'app.cameraAsContent.presenterLoadingLabel' },
  viewerLoadingLabel: { id: 'app.cameraAsContent.viewerLoadingLabel' },
  presenterSharingLabel: { id: 'app.cameraAsContent.presenterSharingLabel' },
  autoplayBlockedDesc: { id: 'app.media.cameraAsContent.autoplayBlockedDesc' },
  autoplayAllowLabel: { id: 'app.media.cameraAsContent.autoplayAllowLabel' },
  started: {
    id: 'app.media.cameraAsContent.start',
    description: 'toast to show when camera as content has started',
  },
  ended: {
    id: 'app.media.cameraAsContent.end',
    description: 'toast to show when camera as content has ended',
  },
  endedDueToDataSaving: {
    id: 'app.media.cameraAsContent.endDueToDataSaving',
    description:
      'toast to show when camera as content has ended by changing data savings option',
  },
});

const ScreenshareContainer = (props) => {
  const screenShare = layoutSelectOutput((i) => i.screenShare);
  const fullscreen = layoutSelect((i) => i.fullscreen);
  const layoutContextDispatch = layoutDispatch();

  const { element } = fullscreen;
  const fullscreenElementId = 'Screenshare';
  const fullscreenContext = element === fullscreenElementId;

  const usingUsersContext = useContext(UsersContext);
  const { users } = usingUsersContext;
  const currentUser = users[Auth.meetingID][Auth.userID];
  const isPresenter = currentUser.presenter;

  const [showModal, setShowModal] = useState(false);

  const handleButtonClick = () => {
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
  };

  const info = {
    screenshare: {
      icon: 'desktop',
      locales: screenshareIntlMessages,
      startPreviewSizeBig: false,
      showSwitchPreviewSizeButton: true,
    },
    camera: {
      icon: 'video',
      locales: cameraAsContentIntlMessages,
      startPreviewSizeBig: true,
      showSwitchPreviewSizeButton: false,
    },
  };

  const getContentType = () => {
    return isPresenter ? getSharingContentType() : getBroadcastContentType();
  };
  const contentTypeInfo = info[getContentType()];
  const defaultInfo = info.camera;
  const selectedInfo = contentTypeInfo ? contentTypeInfo : defaultInfo;

  if (isScreenBroadcasting() || isCameraAsContentBroadcasting()) {
    return (
      <div>
        <div className='top-button'>
          <button onClick={handleButtonClick}>Show Modal</button>
        </div>

        {showModal && (
          <div className='modal-overlay' onClick={closeModal}>
            <div className='modal-content' onClick={(e) => e.stopPropagation()}>
              <p>Hello</p>
              <button onClick={closeModal}>Close</button>
            </div>
          </div>
        )}

        <ScreenshareComponent
          {...{
            layoutContextDispatch,
            ...props,
            ...screenShare,
            fullscreenContext,
            fullscreenElementId,
            isPresenter,
            ...selectedInfo,
          }}
        />

        <style jsx>{`
          .top-button {
            position: absolute;
            top: 0;
            left: 0;
            padding: 10px;
            z-index: 1000;
          }
          .modal-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100vw;
            height: 100vh;
            background-color: rgba(0, 0, 0, 0.5);
            display: flex;
            justify-content: center;
            align-items: center;
          }
          .modal-content {
            background: white;
            padding: 20px;
            border-radius: 8px;
            text-align: center;
          }
        `}</style>
      </div>
    );
  }
  return null;
};

const LAYOUT_CONFIG = Meteor.settings.public.layout;

export default withTracker(() => {
  return {
    isGloballyBroadcasting:
      isScreenGloballyBroadcasting() || isCameraAsContentGloballyBroadcasting(),
    toggleSwapLayout: MediaService.toggleSwapLayout,
    hidePresentationOnJoin: getFromUserSettings(
      'bbb_hide_presentation_on_join',
      LAYOUT_CONFIG.hidePresentationOnJoin
    ),
    enableVolumeControl: shouldEnableVolumeControl(),
    outputDeviceId: AudioService.outputDeviceId(),
    isSharedNotesPinned: MediaService.shouldShowSharedNotes(),
    pinSharedNotes: NotesService.pinSharedNotes,
  };
})(ScreenshareContainer);
