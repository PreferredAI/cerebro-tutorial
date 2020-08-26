import React, { Component } from 'react';
import axios from 'axios'
import './side_by_side.css'
import BeautyStars from 'beauty-stars'

var indexedURL = 'http://localhost:8080/blankapp';


export default class Related extends Component {
    constructor(props){
        super(props);

        this.state = {
            userid : props.location.userid,
            query : "",
            rating: null,
            movie: props.location.item
        };

        this.onChangeQuery = this.onChangeQuery.bind(this);
        this.onChangeRating = this.onChangeRating.bind(this);
        this.onSubmit = this.onSubmit.bind(this);
        const ids = {
            userId : this.state.userid,
            itemId : this.state.movie._id
        };
        axios.post(indexedURL + '/getRating', ids)
            .then(res => {
                this.setState({
                    rating: res.data.rating,
                })
            }).catch(function (err) {
            console.log(err)
        });

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
    onChangeRating(value){
        this.setState({ rating : value});
        const interaction = {
            userID : this.state.userid,
            itemID : this.state.movie._id,
            rating : value
        };
        axios.post(indexedURL + '/setRating', interaction)
            .catch(function (err) {
            console.log(err)
        });
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
                                value={this.state.query}
                                onChange={this.onChangeQuery}
                        />
                    </div>
                    <div className="form-group">
                        <input type="submit" value="Search" className="btn btn-primary" />
                    </div>
                </form>

                <h4>You ({this.state.userid}) rated the movie "{this.state.movie.title}":</h4>
                <BeautyStars
                    value={this.state.rating}
                    onChange={this.onChangeRating}
                />
            </div>
        )
    }
}