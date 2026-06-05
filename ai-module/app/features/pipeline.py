import numpy as np

from app.features.static_features import extract_static_features
from app.features.vectorizer import tfidf_vectorizer
from app.schemas.traffic import TrafficEvent
from urllib.parse import unquote


class FeaturePipeline:
    def extract(self, event: TrafficEvent) -> np.ndarray:
        
        # Unquote the URI to expose actual characters
        full_payload = unquote(event.uri)
        
        # Append body if present
        if event.bodySnippet:
            delimiter = "&" if "?" in full_payload else "?"
            full_payload += delimiter + event.bodySnippet
            
        # Temporarily modify event URI for static feature extractor
        original_uri = event.uri
        event.uri = full_payload
        
        static = extract_static_features(event)
        
        # Restore original URI just in case
        event.uri = original_uri

        # Return only static features. TF-IDF causes false normality
        # when an attack contains common n-grams from the training set.
        return static


feature_pipeline = FeaturePipeline()
