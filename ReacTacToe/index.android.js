'use strict';

var Sneer = require('Sneer');
var RCTDeviceEventEmitter = require('RCTDeviceEventEmitter');
var Subscribable = require('Subscribable')
var React = require('react-native');

var BOARD_SIZE = 3;
var _board;
var _player;
var _waitingForAdversarysMove;

var {
  AppRegistry,
  StyleSheet,
  Text,
  TouchableHighlight,
  ToastAndroid,
  View,
} = React;

function log(message) {
  console.log('REACTACTAG: ' + message);
}

function toast(message) {
  ToastAndroid.show(message, ToastAndroid.SHORT);
}

class Board {
  grid: Array < Array < number >> ;

  constructor(data) {
    if (data !== undefined) {
      Object.assign(this, data);
      return;
    }
    var grid = Array(BOARD_SIZE);
    for (var i = 0; i < BOARD_SIZE; i++) {
      var row = Array(BOARD_SIZE);
      for (var j = 0; j < BOARD_SIZE; j++) {
        row[j] = 0;
      }
      grid[i] = row;
    }
    this.grid = grid;
  }

  mark(row: number, col: number, player: number): Board {
    this.grid[row][col] = player;
    return this;
  }

  hasMark(row: number, col: number): boolean {
    return this.grid[row][col] !== 0;
  }

  winner(): ? number {
    for (var i = 0; i < BOARD_SIZE; i++) {
      if (this.grid[i][0] !== 0 &&
        this.grid[i][0] === this.grid[i][1] &&
        this.grid[i][0] === this.grid[i][2]) {
        return this.grid[i][0];
      }
    }

    for (var i = 0; i < BOARD_SIZE; i++) {
      if (this.grid[0][i] !== 0 &&
        this.grid[0][i] === this.grid[1][i] &&
        this.grid[0][i] === this.grid[2][i]) {
        return this.grid[0][i];
      }
    }

    if (this.grid[0][0] !== 0 &&
      this.grid[0][0] === this.grid[1][1] &&
      this.grid[0][0] === this.grid[2][2]) {
      return this.grid[0][0];
    }

    if (this.grid[0][2] !== 0 &&
      this.grid[0][2] === this.grid[1][1] &&
      this.grid[0][2] === this.grid[2][0]) {
      return this.grid[0][2];
    }

    return null;
  }

  tie(): boolean {
    for (var i = 0; i < BOARD_SIZE; i++) {
      for (var j = 0; j < BOARD_SIZE; j++) {
        if (this.grid[i][j] === 0) {
          return false;
        }
      }
    }
    return this.winner() === null;
  }
}

var Cell = React.createClass({
  cellStyle() {
    switch (this.props.player) {
      case 1:
        return styles.cellX;
      case 2:
        return styles.cellO;
      default:
        return null;
    }
  },

  textStyle() {
    switch (this.props.player) {
      case 1:
        return styles.cellTextX;
      case 2:
        return styles.cellTextO;
      default:
        return {};
    }
  },

  textContents() {
    switch (this.props.player) {
      case 1:
        return 'X';
      case 2:
        return 'O';
      default:
        return '';
    }
  },

  render() {
    return (
      <TouchableHighlight
        onPress={this.props.onPress}
        underlayColor="transparent"
        activeOpacity={0.5}>
        <View style={[styles.cell, this.cellStyle()]}>
          <Text style={[styles.cellText, this.textStyle()]}>
            {this.textContents()}
          </Text>
        </View>
      </TouchableHighlight>
    );
  }
});

var GameEndOverlay = React.createClass({
  render() {
    var board = this.props.board;

    var tie = board.tie();
    var winner = board.winner();
    if (!winner && !tie) {
      return <View />;
    }

    var message;
    if (tie) {
      message = 'It\'s a tie!';
    } else {
      message = (winner === 1 ? 'X' : 'O') + ' wins!';
    }

    return (
      <View style={styles.overlay}>
        <Text style={styles.overlayMessage}>{message}</Text>
        <TouchableHighlight
          onPress={this.props.onClose}
          underlayColor="transparent"
          activeOpacity={0.5}>
          <View style={styles.newGame}>
            <Text style={styles.newGameText}>Ok</Text>
          </View>
        </TouchableHighlight>
      </View>
    );
  }
});

var ReacTacToe = React.createClass({
  mixins: [Subscribable.Mixin],

  componentWillMount: function() {
    Sneer.wasCalledFromConversation(itWas => {
      if (itWas) {
        Sneer.join();
        Sneer.wasStartedByMe(wasStartedByMe => {
          this.setState({
            waitingForAdversarysMove: !wasStartedByMe,
            yourSymbol: (wasStartedByMe ? "X" : "O"),
            adversarysSymbol: (wasStartedByMe ? "O" : "X"),
          });
        });
        this.addListenerOn(RCTDeviceEventEmitter, 'upToDate', this.onUpToDate)
        this.addListenerOn(RCTDeviceEventEmitter, 'message', this.onMessage)
      }
    });
  },

  getInitialState() {
    return {
      board: new Board(),
      player: 1
    };
  },

  handleCellPress(row: number, col: number) {
    if (this.state.waitingForAdversarysMove) {
      return;
    }

    if (this.state.board.hasMark(row, col)) {
      return;
    }

    this.setState({
      board: this.state.board.mark(row, col, this.state.player),
      player: this.nextPlayer(),
    }, () => {
      Sneer.send(JSON.stringify(this.state));
    });
  },

  onMessage(message) {
    _board = new Board(JSON.parse(message.payload).board);
    _player = JSON.parse(message.payload).player;
    _waitingForAdversarysMove = message.wasSentByMe;
  },

  onUpToDate() {
    if (_board !== undefined) {
      this.setState({
        board: _board,
        player: _player,
        waitingForAdversarysMove: _waitingForAdversarysMove,
      });
  }
  },

  componentWillUnmount: function() {
    Sneer.close();
  },

  closeGame() {
    Sneer.finish();
  },

  nextPlayer(): number {
    return this.state.player === 1 ? 2 : 1;
  },

  render() {
    var rows = this.state.board.grid.map((cells, row) =>
      <View key={'row' + row} style={styles.row}>
        {cells.map((player, col) =>
          <Cell
            key={'cell' + col}
            player={player}
            onPress={this.handleCellPress.bind(this, row, col)}
          />
        )}
      </View>
    );

    return (
      <View style={styles.container}>
        <Text style={styles.title}>ReacTacToe</Text>
        <View style={styles.board}>
          {rows}
          <Text
            style={styles.player}>{this.state.waitingForAdversarysMove ? "" : "*"}You: {this.state.yourSymbol}</Text>
          <Text style={styles.player}>{this.state.waitingForAdversarysMove ? "*" : ""}Adversary: {this.state.adversarysSymbol}</Text>
        </View>
        <GameEndOverlay
          board={this.state.board}
          onClose={this.closeGame}
        />
      </View>
    );
  }
});

var styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'white'
  },
  title: {
    fontFamily: 'Chalkduster',
    fontSize: 24,
    marginBottom: 10,
  },
  player: {
    fontFamily: 'Chalkduster',
    fontSize: 16,
    color: 'white',
  },
  board: {
    padding: 5,
    backgroundColor: '#47525d',
    borderRadius: 10,
  },
  row: {
    flexDirection: 'row',
  },

  // CELL

  cell: {
    width: 240 / BOARD_SIZE,
    height: 240 / BOARD_SIZE,
    borderRadius: 5,
    backgroundColor: '#7b8994',
    margin: 15 / BOARD_SIZE,
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  cellX: {
    backgroundColor: '#72d0eb',
  },
  cellO: {
    backgroundColor: '#7ebd26',
  },

  // CELL TEXT

  cellText: {
    borderRadius: 5,
    fontSize: 150 / BOARD_SIZE,
    fontFamily: 'AvenirNext-Bold',
  },
  cellTextX: {
    color: '#19a9e5',
  },
  cellTextO: {
    color: '#b9dc2f',
  },

  // GAME OVER

  overlay: {
    position: 'absolute',
    top: 0,
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: 'rgba(221, 221, 221, 0.5)',
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'center',
  },
  overlayMessage: {
    fontSize: 40,
    marginBottom: 20,
    marginLeft: 20,
    marginRight: 20,
    fontFamily: 'AvenirNext-DemiBold',
    textAlign: 'center',
  },
  newGame: {
    backgroundColor: '#887766',
    padding: 20,
    borderRadius: 5,
  },
  newGameText: {
    color: 'white',
    fontSize: 20,
    fontFamily: 'AvenirNext-DemiBold',
  },
});

AppRegistry.registerComponent('ReacTacToe', () => ReacTacToe);
