import React, { Component } from 'react';
import axios from 'axios'
import './side_by_side.css'

var indexedURL = 'http://localhost:8080/blankapp';

/*
export const Item = props => (
    <tr onClick={function () {
        props.parent.nag.push({
            pathname:"/related",
            userid: props.parent.userid,
            item: props.item
        });
    }}>
        <td>{props.item.title}</td>
        <td>{props.item.genres}</td>
    </tr>
);
 */

export default class Homepage extends Component {
    constructor(props){
        super(props);
        this.state = {
            userid : props.location.userid,
            query: '',
            //index_list: [],
        };

        this.onChangeQuery = this.onChangeQuery.bind(this);
        this.onSubmit = this.onSubmit.bind(this);

/*
        const obj = {
            text : this.state.userid
        };
        axios.post(indexedURL + '/getRecom', obj)
            .then(res => {
                this.setState({
                    index_list: res.data.items,
                })
            }).catch(function (err) {
            console.log(err)
        });
 */


    }

    onSubmit(e){
        e.preventDefault();
        this.props.history.push({
            pathname:"/search",
            userid: this.state.userid,
            data: this.state.query
        });
    }

    onChangeQuery(e){
        this.setState({
            query: e.target.value
        })
    }

    render() {
        return (
            <div>
                <h3 align="center">Welcome {this.state.userid}!!!</h3>
                <form onSubmit={this.onSubmit}>
                    <div className="form-group">
                        <label>Keyword: </label>
                        <input  type="text"
                                className="form-control"
                                value={this.state.query}
                                onChange={this.onChangeQuery}
                        />
                    </div>
                    <div className="form-group">
                        <input type="submit" value="Search" className="btn btn-primary" />
                    </div>
                </form>

            </div>
        )
    }

/*
    indexList(){
        const pushObj = {
            nag : this.props.history,
            userid : this.state.userid
        };
        return this.state.index_list.map(function (currentItem, i) {
            return <Item item={currentItem} key={i} parent={pushObj}/>
        })
    }

 */


}