
'use strict';

var Sneer = require('Sneer')
var RCTDeviceEventEmitter = require('RCTDeviceEventEmitter');
var Subscribable = require('Subscribable')
var SwitchAndroid = require('SwitchAndroid')
var React = require('react-native')
var {
  AppRegistry,
  StyleSheet,
  Text,
  TextInput,
  TouchableHighlight,
  View,
} = React;

function toast(message) {
  Sneer.toast(message, Sneer.SHORT);
}

var Answer = React.createClass({
  render: function () {
    return (
      <View style={styles.flowRight}>
        <View style={{flex: 5}}>
          <TextInput
            placeholder={"Answer " + this.props.id}
            value={this.props.text}
            onChangeText={this.props.onChangeText} />
        </View>
        <View style={{flex: 1, alignSelf: 'stretch', marginTop: 20}}>
          <SwitchAndroid
            value={this.props.correct}
            onValueChange={(value) => this.props.onCorrectChange (value)}/>
        </View>
      </View>
    )
  }
})

var demandis = React.createClass({
  mixins: [Subscribable.Mixin],

  getInitialState: function () {
    return {
      enteringQuestions: true,
      questionDraft: '',
      correct: '',
      answerA: '',
      answerB: '',
      answerC: '',
      answerD: ''
    }
  },

  onMessage: message => toast ('onMessage: ' + message.payload),

  onUpToDate: () => toast ('onUpToDate'),

  componentWillMount: function() {
    console.log ("DEMANDIS: componentWillMount")

    Sneer.wasCalledFromConversation(itWas => {
      if (itWas) {
        Sneer.join()
        this.setState ({enteringQuestions: false})
      }
      else
        toast ('Not from conversation.')
    })

    this.addListenerOn(RCTDeviceEventEmitter, 'upToDate', this.onUpToDate)
    this.addListenerOn(RCTDeviceEventEmitter, 'message', this.onMessage)
  },

  componentWillUnmount: function () {
    console.log ("DEMANDIS: componentWillUnmount")
    Sneer.close ()
  },

  onAddQuestion: function () {
    toast ('question: ' + JSON.stringify (this.state))
    this.setState(this.getInitialState ())
  },

  isValid: function () {
    var {questionDraft, answerA, answerB, answerC, answerD, correct} = this.state
    return questionDraft != ""
      && answerA != ""
      && answerB != ""
      && answerC != ""
      && answerD != ""
      && correct != ""
  },

  renderQuestionForm: function() {
    return (
      <View style={styles.container}>
        <TextInput
          value={this.state.questionDraft}
          placeholder="Enter a new question"
          onChangeText={(text) => this.setState({questionDraft: text})} />
          {["A", "B", "C", "D"].map(id => {
            return <Answer
              id={id}
              correct={this.state.correct == id}
              onCorrectChange={correct => this.setState({correct: id})}
              text={this.state["answer" + id]}
              onChangeText={(text) => this.setState({["answer" + id]: text})} />
          })}
        <TouchableHighlight
          onPress={this.isValid () ? this.onAddQuestion : undefined}
          enabled={this.isValid ()}>
          <Text style={styles.button}>
            DONE
          </Text>
        </TouchableHighlight>
      </View>
    );
  },

  render: function() {
    if (this.state.enteringQuestions)
      return this.renderQuestionForm ()
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome to React Native!
        </Text>
        <Text style={styles.instructions}>
          To get started, edit index.android.js
        </Text>
        <Text style={styles.instructions}>
          Shake or press menu button for dev menu
        </Text>
      </View>
    );
  }
});

var styles = StyleSheet.create({
  flowRight: {
    flexDirection: 'row',
    alignItems: 'center',
    alignSelf: 'stretch'
  },
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
  answerContainer: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center'
  },
  button: {
    textAlign: 'center'
  }
});

AppRegistry.registerComponent('demandis', () => demandis);
