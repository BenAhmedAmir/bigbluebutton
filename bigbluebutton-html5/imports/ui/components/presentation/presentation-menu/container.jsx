import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { withTracker } from 'meteor/react-meteor-data';
import PresentationMenu from './component';
import FullscreenService from '/imports/ui/components/common/fullscreen-button/service';
import Auth from '/imports/ui/services/auth';
import Meetings from '/imports/api/meetings';
import {
  layoutSelect,
  layoutDispatch,
} from '/imports/ui/components/layout/context';
import WhiteboardService from '/imports/ui/components/whiteboard/service';
import UserService from '/imports/ui/components/user-list/service';
import { isSnapshotOfCurrentSlideEnabled } from '/imports/ui/services/features';

const PresentationMenuContainer = (props) => {
  const fullscreen = layoutSelect((i) => i.fullscreen);
  const { element: currentElement, group: currentGroup } = fullscreen;
  const layoutContextDispatch = layoutDispatch();
  const { elementId } = props;
  const isFullscreen = currentElement === elementId;
  const isRTL = layoutSelect((i) => i.isRTL);
  const [showModal, setShowModal] = useState(false);

  const handleButtonClick = () => {
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
  };

  return (
    <div>
      <PresentationMenu
        {...props}
        {...{
          currentElement,
          currentGroup,
          isFullscreen,
          layoutContextDispatch,
          isRTL,
        }}
      />

      <div
        className='fullscreen-hover-button'
        onMouseEnter={() => setShowModal(true)}
      >
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

      <style jsx>{`
        .fullscreen-hover-button {
          position: fixed;
          bottom: 20px;
          right: 20px;
          display: flex;
          opacity: 0.5;
          transition: opacity 0.2s;
        }
        .fullscreen-hover-button:hover {
          opacity: 1;
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
};

export default withTracker((props) => {
  const handleToggleFullscreen = (ref) =>
    FullscreenService.toggleFullScreen(ref);
  const isIphone = !!navigator.userAgent.match(/iPhone/i);
  const meetingId = Auth.meetingID;
  const meetingObject = Meetings.findOne(
    { meetingId },
    { fields: { 'meetingProp.name': 1 } }
  );
  const hasWBAccess = WhiteboardService.hasMultiUserAccess(
    WhiteboardService.getCurrentWhiteboardId(),
    Auth.userID
  );
  const amIPresenter = UserService.isUserPresenter(Auth.userID);

  return {
    ...props,
    allowSnapshotOfCurrentSlide: isSnapshotOfCurrentSlideEnabled(),
    handleToggleFullscreen,
    isIphone,
    isDropdownOpen: Session.get('dropdownOpen'),
    meetingName: meetingObject.meetingProp.name,
    hasWBAccess,
    amIPresenter,
  };
})(PresentationMenuContainer);

PresentationMenuContainer.propTypes = {
  elementId: PropTypes.string.isRequired,
};
