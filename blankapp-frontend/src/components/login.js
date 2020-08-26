import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import TextField from 'material-ui/TextField';
import React, { Component } from 'react';
//import background from '../movielens_demo.png';

class Login extends Component {
    constructor(props){
        super(props);
        this.state={
            userid:'',
        };

        this.handleClick = this.handleClick.bind(this);
    }
    render() {
        return (
            <div>
                <MuiThemeProvider>
                    <div>
                        <h3>Login</h3>

                        <form onSubmit={this.handleClick}>
                            <TextField
                                hintText="A number from 1 to 6040"
                                floatingLabelText="User ID"
                                onChange = {(event,newValue) => this.setState({userid:newValue})}
                            />
                            <br/>
                            <div className="form-group">
                                <input type="submit" value="Submit" className="btn btn-primary" />
                            </div>
                        </form>
                    </div>
                </MuiThemeProvider>
            </div>
        );
    }
    handleClick(){
        this.props.history.push({
            pathname:"/home",
            userid: this.state.userid,
        })
    }
}



export default Login;