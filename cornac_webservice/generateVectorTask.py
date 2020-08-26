from pymongo import MongoClient
from cornac.eval_methods import RatioSplit
from cornac.models import PMF
import cornac.metrics
from cornac import Experiment
import requests

def generateLatentVectors(mongohost, mongoport, cerebro_url, dim):
    mongodb_url = 'mongodb://' + mongohost + ':' + mongoport
    client = MongoClient(mongodb_url)
    cerebro = client.cerebro
    ratings = cerebro.ratings
    users = cerebro.users
    data = []
    i = 0
    for user in users.find():
        userid = user.pop("_id")
        records = ratings.find({"userID": userid})
        itemid = ""
        for record in records:
            if record['itemID'] == itemid:
                continue
            itemid = record['itemID']
            l = (userid, itemid, record['rating'])
            data.insert(i, l)
            i += 1

    ratio_split = RatioSplit(data=data, test_size=0.01, rating_threshold=4.0, seed=5654)
    pmf = PMF(k=dim, max_iter=50, learning_rate=0.001)

    mae = cornac.metrics.MAE()
    rmse = cornac.metrics.RMSE()
    rec_10 = cornac.metrics.Recall(k=10)
    ndcg_10 = cornac.metrics.NDCG(k=10)
    auc = cornac.metrics.AUC()

    exp = Experiment(eval_method=ratio_split,
                     models=[pmf],
                     metrics=[mae, rmse, rec_10, ndcg_10, auc],
                     user_based=True)
    exp.run()

    userid = list(pmf.train_set.user_ids)
    itemid = list(pmf.train_set.item_ids)
    userVec = list(pmf.U)
    itemVec = list(pmf.V)
    print("userid len:" + str(len(userid)))
    print("uservec len:" + str(len(userVec)))
    print("itemid len:" + str(len(itemid)))
    print("itemVec len:" + str(len(itemVec)))
    for (id, vec) in zip(userid, userVec):
        vec = list(vec)
        users.update_one({"_id": id}, {"$set": {"vec": vec}})

    for (id, vec) in zip(itemid, itemVec):
        vec = list(vec)
        cerebro.items.update_one({"_id": id}, {"$set": {"vec": vec}})

    json_msg = {"msg": "update"}
    r = requests.post(url=cerebro_url + '/update/buildIdx', json=json_msg)
    print(r.text)
