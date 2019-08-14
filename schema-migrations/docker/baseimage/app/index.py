from flask import Flask
app = Flask(__name__)
@app.route("/")
def rootApp():
    return "Migration Container is up"
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int("5001"), debug=True)