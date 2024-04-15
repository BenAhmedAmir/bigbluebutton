import React, { useState } from 'react';

const TalkingIndicatorToggle = ({ setEnableTalkingIndicator }) => {
    const [enableTalkingIndicator, setEnableTalkingIndicator] = useState(true);

    const toggleTalkingIndicator = () => {
        setEnableTalkingIndicator(current => !current);
    };

    return (
        <div>
            <label>
                Enable Talking Indicator
                <input
                    type="checkbox"
                    checked={enableTalkingIndicator}
                    onChange={toggleTalkingIndicator}
                />
            </label>
        </div>
    );
};

export default TalkingIndicatorToggle;
