from flask import Flask, request
from multiprocessing import Process
from generateVectorTask import generateLatentVectors


app = Flask(__name__)


@app.route('/generate', methods=['GET'])
def generateVector():
    params = {}
    with open('config') as fp:
        line = fp.readline()
        while line:
            temp = line.split('=')
            params[temp[0]] = temp[1][:-1]
            line = fp.readline()
    p = Process(target=generateLatentVectors, args=(params['mongohost'], params['mongoport'], params['cerebro.url'], int(params['dimension'])))
    p.start()
    return "ok\n"

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000)


