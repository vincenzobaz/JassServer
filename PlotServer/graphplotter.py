import matplotlib as mpl
mpl.use('Agg')
from matplotlib import pyplot as plt
import numpy as np

from flask import Flask
from flask import request


app = Flask(__name__)


def create_bar_graph(json_dict):
    labels    = json_dict['labels']
    counters  = json_dict['values']
    sciper    = json_dict['sciper']
    xlabel    = json_dict['xlabel']
    ylabel    = json_dict['ylabel']
    graph     = json_dict['graph']  
    if (len(labels) == 0 or len(counters) == 0): 
        print("Empty arrays for " + sciper + " graph: " + graph)
        return "empty"
    assert len(labels) == len(counters)
    assert len(labels) != 0
    assert len(counters) != 0

    # Start graph creation
    fig, ax = plt.subplots()
    y_pos = np.arange(len(labels))
    ax.bar(y_pos, counters, align='center', color='#99ccff', linewidth=0)
    ax.set_xticks(y_pos)
    ax.set_yticks(np.arange(max(counters) + 2))
    ax.set_xticklabels(labels)
    ax.set_ylabel(ylabel)
    ax.set_xlabel(xlabel)
    
    # Save plot to file
    #os = open("/plots/" + sciper + "_" + graph + ".svg", 'w')
    #fig.savefig(os, format='svg')
    fig.savefig("/plots/" + sciper + "_" + graph)
    #os.close()
    print("Written svg for " + sciper + " graph:" + graph)
    fig.clf()


def serve(dic):
    for k, v in dic.items():
        create_bar_graph(v)


@app.route("/bars", methods=['POST'])
def servicer():
    json_dict = request.get_json()
    serve(json_dict)
    return "got it!"

if __name__ == "__main__":
    app.run()
