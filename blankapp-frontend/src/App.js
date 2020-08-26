import React, { Component } from "react";
import "bootstrap/dist/css/bootstrap.min.css";
import { BrowserRouter as Router, Route, Link } from "react-router-dom";
import Homepage from "./components/homepage";
import Login from "./components/login"
import SearchText from "./components/searchText";
import Related from "./components/relatedItems"
import logo from './logo.png';

class App extends Component {
  render() {
    return (
        <Router>
            <div className="container">
                <nav className=" navbar-expand-lg navbar-light bg-light">
                    <a className="navbar-brand" href="https://preferred.ai/" target="_blank">
                        <img src={logo} width="189" height="42" alt="preferred.ai"/>
                    </a>
                    <Link to="/" className="navbar-brand">Back to Login</Link>
                </nav>
                <br/>
                <Route path="/" exact component={Login}/>
                <Route path="/home" component={Homepage}/>
                <Route path="/search" component={SearchText}/>
                <Route path="/related" component={Related}/>
            </div>
        </Router>
    );
  }
}

export default App;
