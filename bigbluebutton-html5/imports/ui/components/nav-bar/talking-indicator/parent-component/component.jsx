import React, { useState } from 'react';
import TalkingIndicatorToggle
    from "./../talking-indicator-toggle/component"
import TalkingIndicatorContainer from "../container";

const ParentComponent = () => {
    const [enableTalkingIndicator, setEnableTalkingIndicator] = useState(true);

    return (
        <div>
            <TalkingIndicatorToggle setEnableTalkingIndicator={setEnableTalkingIndicator} />
            <TalkingIndicatorContainer enableTalkingIndicator={enableTalkingIndicator} />
        </div>
    );
};

export default ParentComponent;
