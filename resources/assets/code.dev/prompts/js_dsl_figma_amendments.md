
** When importing from ./**/ui/<component>, translate to a single library component `js.lib.figma`
``
import { Card } from './ui/card';
import { Badge } from './ui/badge';
import { Button } from './ui/button';
import { Input } from './ui/input';
```

%FROM
: (l/script :js
  {:require [[szncampaigncenter.components.ui.card :as c]
             [szncampaigncenter.components.ui.badge :as bg]
             [szncampaigncenter.components.ui.button :as b]
             [szncampaigncenter.components.ui.input :as i]]})
  
  (defn.js Example
    []
    (return
    [:% c/Card
      [:% bg/Badge]
      [:% b/Button]
      [:% i/Input]]))
%TO
: (l/script :js
  {:require [[js.lib.figma :as fg]]})
  
  (defn.js Example
    []
    (return
    [:% fg/Card
      [:% fg/Badge]
      [:% fg/Button]