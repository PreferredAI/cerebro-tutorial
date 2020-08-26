import React, { Component } from 'react';
import axios from 'axios'
import './side_by_side.css'


var indexedURL = 'http://localhost:8080/blankapp';
/*
var exhaustive = 'http://localhost:8080/movies';
*/
//var indexedURL = '/api/indexed/movies';

export const Item = props => (
    <tr onClick={function () {
        props.parent.nag.push({
            pathname:"/related",
            userid: props.parent.userid,
            item: props.item
        });
        //window.location.href= "/related/" + props.movie._id;
    }}>
        <td>{props.item.title}</td>
        <td>{props.item.genres}</td>
    </tr>
);

export default class SearchText extends Component {
    constructor(props){
        super(props);

        this.state = {
            userid : props.location.userid,
            query: props.location.data,
            index_list: []
        };

        this.onChangeQuery = this.onChangeQuery.bind(this);
        this.onSubmit = this.onSubmit.bind(this);

        if(this.state.query != null){
            const obj = {
                text : this.state.query
            };
            axios.post(indexedURL + '/searchTitle', obj)
                .then(res => {
                    this.setState({
                        index_list: res.data.items
                    })
                }).catch(function (err) {
                console.log(err)
            });

        }

    }

    onSubmit(e){
        e.preventDefault();
        const obj = {
            text : this.state.query
        };
        axios.post(indexedURL + '/searchTitle', obj)
            .then(res => {
                this.setState({
                    index_list: res.data.items,
                })
            }).catch(function (err) {
            console.log(err)
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
                <h3 align="left">Search</h3>
                <form onSubmit={this.onSubmit}>
                    <div className="form-group">
                        <label>Keyword: </label>
                        <input  type="text"
                                className="form-control"
                                //value={this.state.query}
                                onChange={this.onChangeQuery}
                        />
                    </div>
                    <div className="form-group">
                        <input type="submit" value="Search" className="btn btn-primary" />
                    </div>
                </form>
                <h4>Search Results:</h4>
                <br/>
                <table className="table table-striped">
                    <thead>
                    <tr>
                        <th>Title</th>
                        <th>Genres</th>
                    </tr>
                    </thead>
                    <tbody>
                    { this.indexList() }
                    </tbody>
                </table>

            </div>
        )
    }

    indexList(){
        const pushObj = {
            nag : this.props.history,
            userid : this.state.userid
        };
        return this.state.index_list.map(function (currentItem, i) {
            return <Item item={currentItem} key={i} parent={pushObj}/>
        })
    }
}