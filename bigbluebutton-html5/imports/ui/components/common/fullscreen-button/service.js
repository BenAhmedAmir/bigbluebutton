import React, { useState, useEffect } from 'react';

function getFullscreenElement() {
  if (document.fullscreenElement) return document.fullscreenElement;
  if (document.webkitFullscreenElement) return document.webkitFullscreenElement;
  if (document.mozFullScreenElement) return document.mozFullScreenElement;
  if (document.msFullscreenElement) return document.msFullscreenElement;
  return null;
}

const isFullScreen = (element) => {
  return getFullscreenElement() && getFullscreenElement() === element;
};

function cancelFullScreen() {
  if (document.exitFullscreen) {
    document.exitFullscreen();
  } else if (document.mozCancelFullScreen) {
    document.mozCancelFullScreen();
  } else if (document.webkitExitFullscreen) {
    document.webkitExitFullscreen();
  }
}

function fullscreenRequest(element) {
  if (element.requestFullscreen) {
    element.requestFullscreen();
  } else if (element.mozRequestFullScreen) {
    element.mozRequestFullScreen();
  } else if (element.webkitRequestFullscreen) {
    element.webkitRequestFullscreen();
  } else if (element.msRequestFullscreen) {
    element.msRequestFullscreen();
  }
  document.activeElement.blur();
  element.focus();
}

const toggleFullScreen = (ref = null) => {
  const element = ref || document.documentElement;
  if (isFullScreen(element)) {
    cancelFullScreen();
  } else {
    fullscreenRequest(element);
  }
};

function FullScreenComponent() {
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    const handleFullscreenChange = () => {
      setIsFullscreen(Boolean(getFullscreenElement()));
    };

    document.addEventListener('fullscreenchange', handleFullscreenChange);
    return () => {
      document.removeEventListener('fullscreenchange', handleFullscreenChange);
    };
  }, []);

  const handleToggleFullscreen = () => {
    toggleFullScreen(document.documentElement);
  };

  return (
    <div>
      <button onClick={handleToggleFullscreen}>
        {isFullscreen ? 'Exit Fullscreen' : 'Enter Fullscreen'}
      </button>

      {isFullscreen && (
        <div
          className="fullscreen-hover-button"
          onMouseEnter={() => setShowModal(true)}
        >
          <button>Show Modal</button>
        </div>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content">
            <p>This is the modal content.</p>
            <button onClick={() => setShowModal(false)}>Close</button>
          </div>
        </div>
      )}

      <style jsx>{`
        .fullscreen-hover-button {
          position: fixed;
          bottom: 20px;
          right: 20px;
          display: flex;
          opacity: 0;
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
}

export default FullScreenComponent;
