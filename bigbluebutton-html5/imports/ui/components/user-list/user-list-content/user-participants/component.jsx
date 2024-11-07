import React, { Component } from 'react';
import { defineMessages } from 'react-intl';
import PropTypes from 'prop-types';
import Styled from './styles';
import { findDOMNode } from 'react-dom';
import { AutoSizer, CellMeasurer, CellMeasurerCache } from 'react-virtualized';
import UserListItemContainer from './user-list-item/container';
import UserOptionsContainer from './user-options/container';
import Settings from '/imports/ui/services/settings';
import { injectIntl } from 'react-intl';

const propTypes = {
  compact: PropTypes.bool,
  intl: PropTypes.shape({
    formatMessage: PropTypes.func.isRequired,
  }).isRequired,
  currentUser: PropTypes.shape({}),
  users: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  setEmojiStatus: PropTypes.func.isRequired,
  clearAllEmojiStatus: PropTypes.func.isRequired,
  clearAllReactions: PropTypes.func.isRequired,
  roving: PropTypes.func.isRequired,
  requestUserInformation: PropTypes.func.isRequired,
};

const defaultProps = {
  compact: false,
  currentUser: null,
};

const intlMessages = defineMessages({
  usersTitle: {
    id: 'app.userList.usersTitle',
    description: 'Title for the Header',
  },
  searchPlaceholder: {
    id: 'app.userList.searchPlaceholder',
    description: 'Placeholder for the search input',
    defaultMessage: 'Search users...',
  },
});

class UserParticipants extends Component {
  constructor() {
    super();

    this.cache = new CellMeasurerCache({
      fixedWidth: true,
      keyMapper: () => 1,
    });

    this.state = {
      selectedUser: null,
      isOpen: false,
      scrollArea: null,
      searchQuery: '',
    };

    this.userRefs = [];

    this.getScrollContainerRef = this.getScrollContainerRef.bind(this);
    this.rove = this.rove.bind(this);
    this.changeState = this.changeState.bind(this);
    this.rowRenderer = this.rowRenderer.bind(this);
    this.handleClickSelectedUser = this.handleClickSelectedUser.bind(this);
    this.selectEl = this.selectEl.bind(this);
    this.handleSearchChange = this.handleSearchChange.bind(this);
  }

  handleSearchChange(event) {
    this.setState({ searchQuery: event.target.value });
  }

  rowRenderer({ index, parent, style, key }) {
    const {
      compact,
      setEmojiStatus,
      setUserAway,
      users,
      requestUserInformation,
      currentUser,
      meetingIsBreakout,
      lockSettingsProps,
      isThisMeetingLocked,
    } = this.props;
    const { scrollArea, searchQuery } = this.state;
    const user = users[index];
    const isRTL = Settings.application.isRTL;

    // Only render if user matches search query
    if (!user.name.toLowerCase().includes(searchQuery.toLowerCase())) {
      return null;
    }

    return (
      <CellMeasurer
        key={key}
        cache={this.cache}
        columnIndex={0}
        parent={parent}
        rowIndex={index}
      >
        <span style={style} key={key} id={`user-${user?.userId || ''}`}>
          <UserListItemContainer
            {...{
              compact,
              setEmojiStatus,
              setUserAway,
              requestUserInformation,
              currentUser,
              meetingIsBreakout,
              scrollArea,
              isRTL,
              lockSettingsProps,
              isThisMeetingLocked,
            }}
            user={user}
            getScrollContainerRef={this.getScrollContainerRef}
          />
        </span>
      </CellMeasurer>
    );
  }

  render() {
    const {
      intl,
      users,
      compact,
      clearAllEmojiStatus,
      clearAllReactions,
      currentUser,
      meetingIsBreakout,
      isMeetingMuteOnStart,
    } = this.props;
    const { isOpen, scrollArea, searchQuery } = this.state;

    // Filtered users based on search query
    const filteredUsers = users.filter((user) =>
      user.name.toLowerCase().includes(searchQuery.toLowerCase())
    );

    return (
      <Styled.UserListColumn data-test='userList'>
        {!compact ? (
          <Styled.Container>
            <Styled.SmallTitle>
              {intl.formatMessage(intlMessages.usersTitle)}
              {users.length > 0 ? ` (${filteredUsers.length})` : null}
            </Styled.SmallTitle>
            <Styled.SearchInput
              type='text'
              value={searchQuery}
              onChange={this.handleSearchChange}
              placeholder={intl.formatMessage(intlMessages.searchPlaceholder)}
            />
            {currentUser?.role === ROLE_MODERATOR ? (
              <UserOptionsContainer
                {...{
                  clearAllEmojiStatus,
                  clearAllReactions,
                  meetingIsBreakout,
                  isMeetingMuteOnStart,
                }}
              />
            ) : null}
          </Styled.Container>
        ) : (
          <Styled.Separator />
        )}
        <Styled.VirtualizedScrollableList
          id={'user-list-virtualized-scroll'}
          aria-label='Users list'
          role='region'
          tabIndex={0}
          ref={(ref) => {
            this.refScrollContainer = ref;
          }}
        >
          <span id='participants-destination' />
          <AutoSizer>
            {({ height, width }) => (
              <Styled.VirtualizedList
                {...{
                  isOpen,
                  users,
                }}
                ref={(ref) => {
                  if (ref !== null) {
                    this.listRef = ref;
                  }

                  if (ref !== null && !scrollArea) {
                    this.setState({ scrollArea: findDOMNode(ref) });
                  }
                }}
                rowHeight={this.cache.rowHeight}
                rowRenderer={this.rowRenderer}
                rowCount={filteredUsers.length || SKELETON_COUNT}
                height={height - 1}
                width={width - 1}
                overscanRowCount={30}
                deferredMeasurementCache={this.cache}
                tabIndex={-1}
              />
            )}
          </AutoSizer>
        </Styled.VirtualizedScrollableList>
      </Styled.UserListColumn>
    );
  }
}

UserParticipants.propTypes = propTypes;
UserParticipants.defaultProps = defaultProps;

export default injectIntl(UserParticipants);
