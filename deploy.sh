if [ -f "deploy.config" ]; then
    cabal run . clean
    cabal run . build
    source deploy.config    
    aws s3 sync --delete _site/ s3://$bucketId
    aws cloudfront create-invalidation --distribution-id $distributionId --paths "/*"
else
    echo "deploy.config file not found."
fi
