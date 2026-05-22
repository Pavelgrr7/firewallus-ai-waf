import numpy as np

from app.features.static_features import extract_static_features
from app.features.vectorizer import tfidf_vectorizer
from app.schemas.traffic import TrafficEvent


class FeaturePipeline:
    def extract(self, event: TrafficEvent) -> np.ndarray:
        static = extract_static_features(event)

        tfidf = tfidf_vectorizer.transform(event.uri)
        if tfidf is not None:
            return np.concatenate([static, tfidf])

        return static


feature_pipeline = FeaturePipeline()
